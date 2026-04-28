package elovaire.music.app.data.playback

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.database.ContentObserver
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioMixerAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.MediaSession
import elovaire.music.app.MainActivity
import elovaire.music.app.domain.model.Album
import elovaire.music.app.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class PlaybackRepeatMode {
    Off,
    One,
    All,
}

data class PlaybackUiState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.Off,
    val shuffleEnabled: Boolean = false,
    val sourceLabel: String? = null,
    val volume: Float = 1f,
    val audioSessionId: Int = 0,
    val recentSongIds: List<Long> = emptyList(),
    val recentAlbumIds: List<Long> = emptyList(),
) {
    val currentSong: Song?
        get() = queue.getOrNull(currentIndex)
}

data class PlaybackProgressState(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

@SuppressLint("UnsafeOptInUsageError")
class PlaybackManager(
    context: Context,
    scope: CoroutineScope,
) {
    private val scope = scope
    private val appContext = context.applicationContext
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val playbackAudioAttributes = AudioAttributes.Builder()
        .setUsage(androidx.media3.common.C.USAGE_MEDIA)
        .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()
    private val platformPlaybackAudioAttributes = android.media.AudioAttributes.Builder()
        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private var userVolume = currentSystemVolumeFraction()
    private var volumeFineGain = 1f
    private var lastKnownSystemVolumeFraction = userVolume
    private var ignoreObservedSystemVolumeStep: Int? = null
    private var usbOutputDevice: AudioDeviceInfo? = null
    private var bitPerfectUsbPlaybackActive = false
    private val extractorsFactory = DefaultExtractorsFactory()
        .setConstantBitrateSeekingEnabled(true)
    private val player = ExoPlayer.Builder(context)
        .setRenderersFactory(
            DefaultRenderersFactory(context)
                .setEnableAudioFloatOutput(true)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER),
        )
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(context, extractorsFactory),
        )
        .setAudioAttributes(playbackAudioAttributes, false)
        .setHandleAudioBecomingNoisy(true)
        .build()
        .apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            platformPlaybackAudioAttributes,
        )
        .setOnAudioFocusChangeListener(::handleAudioFocusChange)
        .setAcceptsDelayedFocusGain(false)
        .build()
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            usbOutputDevice = findUsbOutputDevice()
            refreshUsbBitPerfectMode()
            syncFromObservedSystemVolume()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            usbOutputDevice = findUsbOutputDevice()
            refreshUsbBitPerfectMode()
            syncFromObservedSystemVolume()
        }
    }
    private var lastRecordedSongId: Long? = null
    private var fadeJob: Job? = null
    private var hasAudioFocus = false
    private var isDucked = false
    private var isPauseTransitioningToStopped = false
    private var isManualPausePending = false
    private var shouldResumeAfterTransientFocusLoss = false
    private var duckRecoveryJob: Job? = null
    private var isStoppingQueue = false
    private var isRecoveringPlayback = false
    private var lastKnownQueueIndex = -1
    private var lastKnownPositionMs = 0L
    private val systemVolumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            syncFromObservedSystemVolume()
        }

        override fun onChange(
            selfChange: Boolean,
            uri: android.net.Uri?,
        ) {
            syncFromObservedSystemVolume()
        }
    }

    private val _state = MutableStateFlow(PlaybackUiState(volume = userVolume))
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()
    private val _progressState = MutableStateFlow(PlaybackProgressState())
    val progressState: StateFlow<PlaybackProgressState> = _progressState.asStateFlow()
    val playerInstance: Player
        get() = player
    val mediaSessionToken
        get() = mediaSession.token
    val platformMediaSessionToken
        get() = mediaSession.platformToken
    private val mediaSession = MediaSession.Builder(context, player)
        .setSessionActivity(
            PendingIntent.getActivity(
                context,
                3101,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_OPEN_PLAYER_FROM_NOTIFICATION, true)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
        .build()

    init {
        appContext.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            systemVolumeObserver,
        )
        audioManager?.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        usbOutputDevice = findUsbOutputDevice()
        syncFromObservedSystemVolume()
        player.volume = effectivePlayerGain()
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED && player.repeatMode == Player.REPEAT_MODE_OFF) {
                        stopAndClearQueue()
                    } else if (
                        playbackState == Player.STATE_IDLE &&
                        !isStoppingQueue &&
                        _state.value.queue.isNotEmpty() &&
                        !isRecoveringPlayback
                    ) {
                        recoverUnexpectedIdleState(shouldAutoPlay = _state.value.isPlaying || player.playWhenReady)
                    } else {
                        updateState()
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    if (_state.value.queue.isNotEmpty() && !isStoppingQueue && !isRecoveringPlayback) {
                        recoverUnexpectedIdleState(shouldAutoPlay = _state.value.isPlaying || player.playWhenReady)
                    } else {
                        updateState()
                    }
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    updateState()
                }
            },
        )

        scope.launch {
            while (isActive) {
                updateProgressState()
                delay(if (player.isPlaying || player.currentMediaItemIndex >= 0) 250L else 1000L)
            }
        }
    }

    fun playSong(
        song: Song,
        collection: List<Song>,
        sourceLabel: String? = song.album,
        shuffleEnabled: Boolean = false,
    ) {
        val startIndex = collection.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        setQueue(collection, startIndex, sourceLabel, shuffleEnabled)
    }

    fun playAlbum(
        album: Album,
        startSongId: Long? = null,
        sourceLabel: String? = album.title,
        shuffleEnabled: Boolean = false,
    ) {
        val startIndex = if (startSongId == null) {
            0
        } else {
            album.songs.indexOfFirst { it.id == startSongId }.coerceAtLeast(0)
        }
        setQueue(album.songs, startIndex, sourceLabel, shuffleEnabled)
    }

    fun togglePlayback() {
        shouldResumeAfterTransientFocusLoss = false
        if (isManualPausePending) {
            fadeJob?.cancel()
            fadeJob = null
            isManualPausePending = false
            isPauseTransitioningToStopped = false
            resumePlayback()
        } else if (player.isPlaying) {
            isPauseTransitioningToStopped = true
            isManualPausePending = true
            fadeOutAndPause()
        } else {
            isManualPausePending = false
            isPauseTransitioningToStopped = false
            resumePlayback()
        }
        updateState()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceAtLeast(0L))
        updateState()
    }

    fun setVolume(volume: Float) {
        if (shouldBypassSystemStreamVolume()) {
            userVolume = volume.quantizedVolume()
            volumeFineGain = userVolume
            player.volume = effectivePlayerGain()
            updateState()
            return
        }
        applyFineGrainedVolume(volume.quantizedVolume())
        player.volume = effectivePlayerGain()
        updateState()
    }

    fun cycleRepeatMode() {
        player.repeatMode = when (_state.value.repeatMode) {
            PlaybackRepeatMode.Off -> Player.REPEAT_MODE_ONE
            PlaybackRepeatMode.One -> Player.REPEAT_MODE_ALL
            PlaybackRepeatMode.All -> Player.REPEAT_MODE_OFF
        }
        updateState()
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
        updateState()
    }

    fun skipNext() {
        shouldResumeAfterTransientFocusLoss = false
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            stopAndClearQueue()
        }
        updateState()
    }

    fun skipPrevious() {
        shouldResumeAfterTransientFocusLoss = false
        if (player.currentPosition > PREVIOUS_SEEK_THRESHOLD_MS) {
            player.seekTo(0)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            stopAndClearQueue()
        }
        updateState()
    }

    fun playQueueIndex(index: Int) {
        if (index !in _state.value.queue.indices) return
        shouldResumeAfterTransientFocusLoss = false
        player.seekToDefaultPosition(index)
        if (requestAudioFocus()) {
            player.volume = effectivePlayerGain()
            player.playWhenReady = true
            player.play()
        }
        updateState()
    }

    fun enqueueSong(song: Song) {
        val existingQueue = _state.value.queue
        if (existingQueue.isEmpty() || player.mediaItemCount == 0) {
            playSong(song = song, collection = listOf(song), sourceLabel = song.album)
            return
        }
        player.addMediaItem(
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .build(),
        )
        _state.value = _state.value.copy(queue = existingQueue + song)
        updateState()
    }

    fun release() {
        fadeJob?.cancel()
        duckRecoveryJob?.cancel()
        clearUsbBitPerfectMode()
        abandonAudioFocus()
        appContext.contentResolver.unregisterContentObserver(systemVolumeObserver)
        audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
        mediaSession.release()
        player.release()
    }

    private fun setQueue(
        songs: List<Song>,
        startIndex: Int,
        sourceLabel: String?,
        shuffleEnabled: Boolean,
    ) {
        if (songs.isEmpty()) return
        shouldResumeAfterTransientFocusLoss = false

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.uri)
                .build()
        }

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.shuffleModeEnabled = shuffleEnabled
        player.prepare()
        if (requestAudioFocus()) {
            player.volume = effectivePlayerGain()
            player.playWhenReady = true
        } else {
            player.playWhenReady = false
        }
        _state.value = _state.value.copy(queue = songs, sourceLabel = sourceLabel)
        refreshUsbBitPerfectMode(song = songs.getOrNull(startIndex))
        updateState()
    }

    private fun stopAndClearQueue() {
        isStoppingQueue = true
        fadeJob?.cancel()
        fadeJob = null
        shouldResumeAfterTransientFocusLoss = false
        player.pause()
        player.playWhenReady = false
        player.seekTo(0L)
        player.clearMediaItems()
        isManualPausePending = false
        isPauseTransitioningToStopped = false
        clearUsbBitPerfectMode()
        abandonAudioFocus()
        lastRecordedSongId = null
        _state.value = _state.value.copy(
            queue = emptyList(),
            currentIndex = -1,
            isPlaying = false,
            sourceLabel = null,
            audioSessionId = 0,
        )
        _progressState.value = PlaybackProgressState()
        isStoppingQueue = false
    }

    private fun updateState() {
        val existingState = _state.value
        val currentIndex = player.currentMediaItemIndex.takeIf { it >= 0 } ?: -1
        val currentSong = existingState.queue.getOrNull(currentIndex)
        if (currentIndex >= 0) {
            lastKnownQueueIndex = currentIndex
            lastKnownPositionMs = player.currentPosition.coerceAtLeast(0L)
        }
        val hasNewSong = currentSong != null && currentSong.id != lastRecordedSongId
        val recentSongIds = if (hasNewSong) {
            lastRecordedSongId = currentSong.id
            pushRecentId(currentSong.id, existingState.recentSongIds)
        } else {
            existingState.recentSongIds
        }
        val recentAlbumIds = if (hasNewSong) {
            pushRecentId(currentSong.albumId, existingState.recentAlbumIds)
        } else {
            existingState.recentAlbumIds
        }

        if (currentSong == null) {
            lastRecordedSongId = null
        }
        refreshUsbBitPerfectMode(currentSong)
        userVolume = currentEffectiveVolumeFraction()

        val updatedState = existingState.copy(
            currentIndex = currentIndex,
            isPlaying = if (isPauseTransitioningToStopped) false else player.isPlaying,
            repeatMode = player.repeatMode.toPlaybackRepeatMode(),
            shuffleEnabled = player.shuffleModeEnabled,
            sourceLabel = existingState.sourceLabel ?: currentSong?.album,
            volume = userVolume,
            audioSessionId = player.audioSessionId.takeIf { it > 0 } ?: 0,
            recentSongIds = recentSongIds,
            recentAlbumIds = recentAlbumIds,
        )
        if (updatedState != existingState) {
            _state.value = updatedState
        }
        updateProgressState()
    }

    private fun updateProgressState() {
        val updatedProgress = PlaybackProgressState(
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0 }?.coerceAtLeast(0L) ?: 0L,
        )
        if (updatedProgress != _progressState.value) {
            _progressState.value = updatedProgress
        }
    }

    private fun resumePlayback() {
        fadeJob?.cancel()
        fadeJob = null
        isManualPausePending = false
        if (!requestAudioFocus()) return
        isDucked = false
        isPauseTransitioningToStopped = false
        shouldResumeAfterTransientFocusLoss = false
        player.volume = effectivePlayerGain()
        player.play()
    }

    private fun fadeOutAndPause(
        durationMs: Long = PAUSE_FADE_DURATION_MS,
        abandonFocusAfterPause: Boolean = true,
    ) {
        val wasPlaying = player.isPlaying || player.playWhenReady
        if (!wasPlaying) {
            player.pause()
            isManualPausePending = false
            isPauseTransitioningToStopped = false
            player.volume = effectivePlayerGain()
            if (abandonFocusAfterPause) abandonAudioFocus()
            updateState()
            return
        }

        animateVolumeTo(
            targetVolume = 0f,
            durationMs = durationMs,
        ) {
            player.pause()
            isManualPausePending = false
            isPauseTransitioningToStopped = false
            player.volume = effectivePlayerGain()
            if (abandonFocusAfterPause) abandonAudioFocus()
            updateState()
        }
    }

    private fun fadeToDuckedVolume() {
        if (!player.isPlaying) return
        duckRecoveryJob?.cancel()
        isDucked = true
        animateVolumeTo(
            targetVolume = effectivePlayerGain(),
            durationMs = DUCK_FADE_DURATION_MS,
        ) {
            updateState()
        }
        duckRecoveryJob = scope.launch {
            delay(DUCK_RECOVERY_TIMEOUT_MS)
            if (isDucked) {
                restoreFromDuck()
            }
        }
    }

    private fun restoreFromDuck() {
        if (!isDucked) return
        duckRecoveryJob?.cancel()
        duckRecoveryJob = null
        isDucked = false
        animateVolumeTo(
            targetVolume = effectivePlayerGain(),
            durationMs = DUCK_FADE_DURATION_MS,
        ) {
            updateState()
        }
    }

    private fun animateVolumeTo(
        targetVolume: Float,
        durationMs: Long,
        onComplete: (() -> Unit)? = null,
    ) {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val startVolume = player.volume.coerceIn(0f, 1f)
            val endVolume = targetVolume.coerceIn(0f, 1f)
            val steps = (durationMs / FADE_STEP_MS).toInt().coerceAtLeast(1)
            repeat(steps) { step ->
                val fraction = (step + 1f) / steps.toFloat()
                player.volume = startVolume + ((endVolume - startVolume) * fraction)
                delay((durationMs / steps).coerceAtLeast(1L))
            }
            player.volume = endVolume
            fadeJob = null
            onComplete?.invoke()
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true
        val result = audioManager?.requestAudioFocus(audioFocusRequest)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        audioManager?.abandonAudioFocusRequest(audioFocusRequest)
        hasAudioFocus = false
        isDucked = false
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                duckRecoveryJob?.cancel()
                duckRecoveryJob = null
                hasAudioFocus = true
                if (shouldResumeAfterTransientFocusLoss && _state.value.queue.isNotEmpty()) {
                    resumePlayback()
                } else {
                    restoreFromDuck()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                fadeToDuckedVolume()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                duckRecoveryJob?.cancel()
                duckRecoveryJob = null
                shouldResumeAfterTransientFocusLoss = player.isPlaying || player.playWhenReady
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                fadeOutAndPause(
                    durationMs = INTERRUPTION_FADE_DURATION_MS,
                    abandonFocusAfterPause = false,
                )
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                duckRecoveryJob?.cancel()
                duckRecoveryJob = null
                shouldResumeAfterTransientFocusLoss = false
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                fadeOutAndPause(
                    durationMs = INTERRUPTION_FADE_DURATION_MS,
                    abandonFocusAfterPause = true,
                )
            }
        }
    }

    private fun recoverUnexpectedIdleState(shouldAutoPlay: Boolean) {
        val snapshot = _state.value
        if (snapshot.queue.isEmpty()) return
        val recoverIndex = lastKnownQueueIndex
            .takeIf { it in snapshot.queue.indices }
            ?: snapshot.currentIndex.takeIf { it in snapshot.queue.indices }
            ?: 0
        val recoverPosition = lastKnownPositionMs.coerceAtLeast(0L)
        isRecoveringPlayback = true
        fadeJob?.cancel()
        fadeJob = null
        scope.launch {
            val mediaItems = snapshot.queue.map { song ->
                MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(song.uri)
                    .build()
            }
            player.setMediaItems(mediaItems, recoverIndex, recoverPosition)
            player.shuffleModeEnabled = snapshot.shuffleEnabled
            player.repeatMode = snapshot.repeatMode.toPlayerRepeatMode()
            player.prepare()
            if (shouldAutoPlay && requestAudioFocus()) {
                player.volume = effectivePlayerGain()
                player.playWhenReady = true
                player.play()
            }
            isRecoveringPlayback = false
            updateState()
        }
    }

    private fun effectivePlayerGain(): Float {
        if (bitPerfectUsbPlaybackActive) return 1f
        val duckMultiplier = if (isDucked) DUCKED_VOLUME_MULTIPLIER else 1f
        val baseGain = if (usesFixedVolumeOutput()) userVolume else volumeFineGain
        return (baseGain * duckMultiplier).coerceIn(0f, 1f)
    }

    private fun applyFineGrainedVolume(targetVolume: Float) {
        if (bitPerfectUsbPlaybackActive) {
            userVolume = targetVolume
            player.volume = 1f
            return
        }
        if (usesFixedVolumeOutput()) {
            userVolume = targetVolume
            volumeFineGain = targetVolume
            player.volume = effectivePlayerGain()
            return
        }
        val manager = audioManager
        if (manager == null) {
            userVolume = targetVolume
            volumeFineGain = targetVolume
            return
        }
        val maxStep = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        if (targetVolume <= 0f) {
            ignoreObservedSystemVolumeStep = 0
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
            volumeFineGain = 0f
            userVolume = 0f
            return
        }

        val exactSteps = targetVolume * maxStep.toFloat()
        val targetSystemStep = ceil(exactSteps).toInt().coerceIn(1, maxStep)
        ignoreObservedSystemVolumeStep = targetSystemStep
        manager.setStreamVolume(AudioManager.STREAM_MUSIC, targetSystemStep, 0)
        volumeFineGain = (exactSteps / targetSystemStep.toFloat()).coerceIn(0f, 1f)
        userVolume = currentEffectiveVolumeFraction()
    }

    private fun syncFromObservedSystemVolume() {
        if (bitPerfectUsbPlaybackActive) {
            ignoreObservedSystemVolumeStep = null
            lastKnownSystemVolumeFraction = currentSystemVolumeFraction()
            player.volume = 1f
            updateState()
            return
        }
        if (usesFixedVolumeOutput()) {
            ignoreObservedSystemVolumeStep = null
            lastKnownSystemVolumeFraction = currentSystemVolumeFraction()
            player.volume = effectivePlayerGain()
            updateState()
            return
        }
        val observedSystemStep = currentSystemVolumeStep()
        if (ignoreObservedSystemVolumeStep == observedSystemStep) {
            ignoreObservedSystemVolumeStep = null
            userVolume = currentEffectiveVolumeFraction()
        } else {
            volumeFineGain = if (observedSystemStep <= 0) 0f else 1f
            userVolume = currentSystemVolumeFraction().quantizedVolume()
        }
        lastKnownSystemVolumeFraction = currentSystemVolumeFraction()
        player.volume = effectivePlayerGain()
        updateState()
    }

    private fun currentEffectiveVolumeFraction(): Float {
        if (shouldBypassSystemStreamVolume()) return userVolume
        val currentSystemFraction = currentSystemVolumeFraction()
        return (currentSystemFraction * volumeFineGain).coerceIn(0f, 1f).quantizedVolume()
    }

    private fun shouldBypassSystemStreamVolume(): Boolean {
        return bitPerfectUsbPlaybackActive || usesFixedVolumeOutput()
    }

    private fun usesFixedVolumeOutput(): Boolean {
        return audioManager?.isVolumeFixed == true
    }

    private fun refreshUsbBitPerfectMode(song: Song? = _state.value.currentSong) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            bitPerfectUsbPlaybackActive = false
            return
        }
        val manager = audioManager ?: run {
            bitPerfectUsbPlaybackActive = false
            return
        }
        val device = findUsbOutputDevice().also { usbOutputDevice = it } ?: run {
            clearUsbBitPerfectMode()
            return
        }
        val preferredMixer = song
            ?.let(::buildPreferredUsbMixerAttributes)
            ?.let { preferred ->
                manager.getSupportedMixerAttributes(device).firstOrNull { supported ->
                    supported.mixerBehavior == AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT &&
                        supported.matchesPlaybackFormat(preferred.format)
                }
            }

        if (preferredMixer == null) {
            clearUsbBitPerfectMode()
            return
        }

        val applied = manager.setPreferredMixerAttributes(
            platformPlaybackAudioAttributes,
            device,
            preferredMixer,
        )
        bitPerfectUsbPlaybackActive = applied
        if (applied) {
            volumeFineGain = 1f
            userVolume = lastKnownSystemVolumeFraction.quantizedVolume()
        }
        player.volume = effectivePlayerGain()
    }

    private fun clearUsbBitPerfectMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val manager = audioManager
            val device = usbOutputDevice
            if (manager != null && device != null) {
                runCatching {
                    manager.clearPreferredMixerAttributes(platformPlaybackAudioAttributes, device)
                }
            }
        }
        bitPerfectUsbPlaybackActive = false
        player.volume = effectivePlayerGain()
    }

    private fun findUsbOutputDevice(): AudioDeviceInfo? {
        val manager = audioManager ?: return null
        return manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { device ->
                device.isSink && (
                    device.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                        device.type == AudioDeviceInfo.TYPE_USB_HEADSET
                    )
            }
    }

    private fun buildPreferredUsbMixerAttributes(song: Song): AudioMixerAttributes? {
        val playbackFormat = buildUsbPlaybackFormat(song) ?: return null
        return AudioMixerAttributes.Builder(playbackFormat)
            .setMixerBehavior(AudioMixerAttributes.MIXER_BEHAVIOR_BIT_PERFECT)
            .build()
    }

    private fun buildUsbPlaybackFormat(song: Song): AudioFormat? {
        val normalizedFormat = song.audioFormat.uppercase()
        if (normalizedFormat !in BIT_PERFECT_USB_COMPATIBLE_FORMATS) return null
        val parsedQuality = parsePlaybackQuality(song.audioQuality) ?: return null
        val encoding = when (parsedQuality.bitDepth) {
            16 -> AudioFormat.ENCODING_PCM_16BIT
            24 -> AudioFormat.ENCODING_PCM_24BIT_PACKED
            32 -> AudioFormat.ENCODING_PCM_32BIT
            else -> return null
        }
        return AudioFormat.Builder()
            .setEncoding(encoding)
            .setSampleRate(parsedQuality.sampleRateHz)
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()
    }

    private fun parsePlaybackQuality(quality: String?): ParsedPlaybackQuality? {
        val match = QUALITY_REGEX.matchEntire(quality.orEmpty()) ?: return null
        val bitDepth = match.groupValues[1].toIntOrNull() ?: return null
        val sampleRateKhz = match.groupValues[2].toFloatOrNull() ?: return null
        return ParsedPlaybackQuality(
            bitDepth = bitDepth,
            sampleRateHz = (sampleRateKhz * 1000f).roundToInt(),
        )
    }

    private fun currentSystemVolumeStep(): Int {
        val manager = audioManager ?: return 0
        return manager.getStreamVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(0)
    }

    private fun currentSystemVolumeFraction(): Float {
        val manager = audioManager ?: return 1f
        val maxStep = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val currentStep = currentSystemVolumeStep()
        return currentStep.toFloat() / maxStep.toFloat()
    }

    private fun pushRecentId(
        id: Long,
        existing: List<Long>,
    ): List<Long> {
        return buildList {
            add(id)
            existing.asSequence()
                .filter { it != id }
                .take(MAX_HISTORY_ITEMS - 1)
                .forEach(::add)
        }
    }

    private companion object {
        const val PREVIOUS_SEEK_THRESHOLD_MS = 5_000L
        const val MAX_HISTORY_ITEMS = 12
        const val PAUSE_FADE_DURATION_MS = 500L
        const val INTERRUPTION_FADE_DURATION_MS = 1_100L
        const val DUCK_FADE_DURATION_MS = 280L
        const val DUCK_RECOVERY_TIMEOUT_MS = 1_800L
        const val FADE_STEP_MS = 40L
        const val DUCKED_VOLUME_MULTIPLIER = 0.35f
        val QUALITY_REGEX = Regex("""(\d{1,2})/(\d{1,3}(?:\.\d)?)kHz""", RegexOption.IGNORE_CASE)
        val BIT_PERFECT_USB_COMPATIBLE_FORMATS = setOf("FLAC", "WAV", "AIFF", "ALAC")
    }
}

private data class ParsedPlaybackQuality(
    val bitDepth: Int,
    val sampleRateHz: Int,
)

private fun AudioMixerAttributes.matchesPlaybackFormat(format: AudioFormat): Boolean {
    val candidateFormat = this.format
    return candidateFormat.encoding == format.encoding &&
        candidateFormat.sampleRate == format.sampleRate &&
        candidateFormat.channelMask == format.channelMask
}

private fun Float.quantizedVolume(): Float {
    return ((coerceIn(0f, 1f) * 100f).roundToInt() / 100f).coerceIn(0f, 1f)
}

    private fun Int.toPlaybackRepeatMode(): PlaybackRepeatMode {
        return when (this) {
            Player.REPEAT_MODE_ONE -> PlaybackRepeatMode.One
            Player.REPEAT_MODE_ALL -> PlaybackRepeatMode.All
            else -> PlaybackRepeatMode.Off
        }
    }

    private fun PlaybackRepeatMode.toPlayerRepeatMode(): Int {
        return when (this) {
            PlaybackRepeatMode.Off -> Player.REPEAT_MODE_OFF
            PlaybackRepeatMode.One -> Player.REPEAT_MODE_ONE
            PlaybackRepeatMode.All -> Player.REPEAT_MODE_ALL
        }
    }
