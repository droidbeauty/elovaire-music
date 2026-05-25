package elovaire.music.app.data.playback

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.database.ContentObserver
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
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
    val transportShowsPause: Boolean = false,
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

@SuppressLint("UnsafeOptInUsageError")
class PlaybackManager(
    context: Context,
    scope: CoroutineScope,
    audioProcessorsProvider: () -> Array<AudioProcessor> = { emptyArray() },
    hasSignalAlteringEffects: () -> Boolean = { false },
    initialRecentSongIds: List<Long> = emptyList(),
    initialRecentAlbumIds: List<Long> = emptyList(),
    onRecentPlaybackChanged: (songIds: List<Long>, albumIds: List<Long>) -> Unit = { _, _ -> },
) {
    private val scope = scope
    private val appContext = context.applicationContext
    private val audioProcessorsProvider = audioProcessorsProvider
    private val hasSignalAlteringEffects = hasSignalAlteringEffects
    private val onRecentPlaybackChanged = onRecentPlaybackChanged
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val usbManager = context.getSystemService(UsbManager::class.java)
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
    private var ignoreObservedSystemVolumeStep: Int? = null
    private val usbDacHardwareVolumeManager = UsbDacHardwareVolumeManager(
        context = appContext,
        audioManager = audioManager,
        usbManager = usbManager,
    )
    private val bitPerfectUsbManager = BitPerfectUsbManager(
        context = appContext,
        audioManager = audioManager,
        playbackAudioAttributes = platformPlaybackAudioAttributes,
    )
    private val extractorsFactory = DefaultExtractorsFactory()
        .setConstantBitrateSeekingEnabled(true)
    private var isDirectPlaybackActive = false
    private var isSwitchingAudioPath = false
    private var player = createPlayer(enableSignalProcessing = true)
    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            platformPlaybackAudioAttributes,
        )
        .setOnAudioFocusChangeListener(::handleAudioFocusChange)
        .setAcceptsDelayedFocusGain(false)
        .build()
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            if (addedDevices.hasUsbOutputDeviceChange()) {
                hasUsbOutputRoute = currentUsbOutputDescriptor() != null
                usbDacHardwareVolumeManager.updateAudioOutputDevice(currentUsbOutputDescriptor())
                bitPerfectUsbManager.refreshConnectedDevices()
                applyPreferredAudioDevice()
                maybeRebuildPlayerForAudioPath()
            }
            syncFromObservedSystemVolume()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            if (removedDevices.hasUsbOutputDeviceChange()) {
                hasUsbOutputRoute = currentUsbOutputDescriptor() != null
                usbDacHardwareVolumeManager.updateAudioOutputDevice(currentUsbOutputDescriptor())
                bitPerfectUsbManager.refreshConnectedDevices()
                applyPreferredAudioDevice()
                maybeRebuildPlayerForAudioPath()
            }
            syncFromObservedSystemVolume()
        }
    }
    private var lastRecordedSongId: Long? = null
    private var hasAudioFocus = false
    private var isPauseTransitioningToStopped = false
    private var isManualPausePending = false
    private var shouldResumeAfterTransientFocusLoss = false
    private var isStoppingQueue = false
    private var isRecoveringPlayback = false
    private var lastKnownQueueIndex = -1
    private var lastKnownPositionMs = 0L
    private var hasUsbOutputRoute = false
    private val playbackProgressController = PlaybackProgressController()
    private var progressUpdateJob: Job? = null
    private var pauseFadeJob: Job? = null
    private val _playerInstanceVersion = MutableStateFlow(0L)
    val playerInstanceVersion: StateFlow<Long> = _playerInstanceVersion.asStateFlow()
    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            bitPerfectUsbManager.clearForStop()
            maybeRebuildPlayerForAudioPath()
            updateState()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            updateState()
        }

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
    }
    private val playerAnalyticsListener = object : AnalyticsListener {
        override fun onAudioTrackInitialized(
            eventTime: AnalyticsListener.EventTime,
            audioTrackConfig: androidx.media3.exoplayer.audio.AudioSink.AudioTrackConfig,
        ) {
            bitPerfectUsbManager.updateCurrentAudioTrackConfig(audioTrackConfig)
            applyPreferredAudioDevice()
            maybeRebuildPlayerForAudioPath()
            player.volume = effectivePlayerGain()
            syncFromObservedSystemVolume()
        }

        override fun onAudioSinkError(
            eventTime: AnalyticsListener.EventTime,
            audioSinkError: Exception,
        ) {
            player.volume = effectivePlayerGain()
            updateState()
        }
    }
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

    private val _state = MutableStateFlow(
        PlaybackUiState(
            volume = userVolume,
            recentSongIds = initialRecentSongIds.distinct(),
            recentAlbumIds = initialRecentAlbumIds.distinct(),
        ),
    )
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()
    private val _progressState = MutableStateFlow(PlaybackProgressState())
    val progressState: StateFlow<PlaybackProgressState> = _progressState.asStateFlow()
    private val _manualPlaybackStartVersion = MutableStateFlow(0L)
    val manualPlaybackStartVersion: StateFlow<Long> = _manualPlaybackStartVersion.asStateFlow()
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
        hasUsbOutputRoute = currentUsbOutputDescriptor() != null
        bitPerfectUsbManager.updateEffectsActive(hasSignalAlteringEffects())
        bitPerfectUsbManager.refreshConnectedDevices()
        appContext.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            systemVolumeObserver,
            )
        audioManager?.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        usbDacHardwareVolumeManager.updateAudioOutputDevice(currentUsbOutputDescriptor())
        applyPreferredAudioDevice()
        attachPlayerObservers(player)
        syncFromObservedSystemVolume()
        player.volume = effectivePlayerGain()

        progressUpdateJob = scope.launch {
            while (isActive) {
                syncFromObservedSystemVolume()
                publishProgressSnapshot()
                delay(
                    when {
                        player.isPlaying -> 220L
                        player.currentMediaItemIndex >= 0 -> 320L
                        else -> 500L
                    },
                )
            }
        }
    }

    fun reevaluateAudioOutputPath() {
        bitPerfectUsbManager.updateEffectsActive(hasSignalAlteringEffects())
        bitPerfectUsbManager.refreshConnectedDevices()
        applyPreferredAudioDevice()
        maybeRebuildPlayerForAudioPath()
        player.volume = targetPlayerOutputGain()
        updateState()
    }

    fun playSong(
        song: Song,
        collection: List<Song>,
        sourceLabel: String? = song.album,
        shuffleEnabled: Boolean = false,
    ) {
        recordManualPlaybackStart()
        val startIndex = collection.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        setQueue(collection, startIndex, sourceLabel, shuffleEnabled)
    }

    private fun createPlayer(enableSignalProcessing: Boolean): ExoPlayer {
        val configuredPlayer = ExoPlayer.Builder(appContext)
            .setRenderersFactory(
                ElovaireRenderersFactory(
                    appContext,
                    if (enableSignalProcessing) audioProcessorsProvider() else emptyArray(),
                )
                    .setEnableAudioFloatOutput(false)
                    .setEnableDecoderFallback(true)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER),
            )
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(appContext, extractorsFactory),
            )
            .setAudioAttributes(playbackAudioAttributes, false)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_OFF
            }
        bitPerfectUsbManager.preferredOutputDevice()?.let(configuredPlayer::setPreferredAudioDevice)
        return configuredPlayer
    }

    private fun attachPlayerObservers(target: ExoPlayer) {
        target.addAnalyticsListener(playerAnalyticsListener)
        target.addListener(playerListener)
    }

    private fun detachPlayerObservers(target: ExoPlayer) {
        target.removeAnalyticsListener(playerAnalyticsListener)
        target.removeListener(playerListener)
    }

    fun playAlbum(
        album: Album,
        startSongId: Long? = null,
        sourceLabel: String? = album.title,
        shuffleEnabled: Boolean = false,
    ) {
        recordManualPlaybackStart()
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
            cancelPauseFade()
            isManualPausePending = false
            isPauseTransitioningToStopped = false
            recordManualPlaybackStart()
            resumePlayback()
        } else if (player.isPlaying) {
            isPauseTransitioningToStopped = true
            isManualPausePending = true
            beginManualPauseFadeOut()
        } else {
            cancelPauseFade()
            isManualPausePending = false
            isPauseTransitioningToStopped = false
            recordManualPlaybackStart()
            resumePlayback()
        }
        updateState()
    }

    fun seekTo(positionMs: Long) {
        _progressState.value = playbackProgressController.cancelScrub()
        player.seekTo(positionMs.coerceAtLeast(0L))
        publishProgressSnapshot()
        updateState()
    }

    fun beginScrub() {
        _progressState.value = playbackProgressController.beginScrub()
    }

    fun updateScrubPosition(positionMs: Long) {
        _progressState.value = playbackProgressController.updateScrubPosition(positionMs)
    }

    fun finishScrub(positionMs: Long) {
        val result = playbackProgressController.finishScrub(positionMs)
        _progressState.value = result.state
        result.seekPositionMs?.let(player::seekTo)
    }

    fun cancelScrub() {
        _progressState.value = playbackProgressController.cancelScrub()
    }

    fun setVolume(volume: Float) {
        val requestedVolume = volume.quantizedVolume()
        if (usbDacHardwareVolumeManager.shouldOwnVolumeControls()) {
            val handled = usbDacHardwareVolumeManager.setHardwareVolume(requestedVolume)
            if (handled || usbDacHardwareVolumeManager.shouldOwnVolumeControls()) {
                userVolume = usbDacHardwareVolumeManager.currentHardwareVolume() ?: requestedVolume
                player.volume = effectivePlayerGain()
                updateState()
                return
            }
        }
        if (shouldBypassSystemStreamVolume()) {
            userVolume = requestedVolume
            volumeFineGain = userVolume
            player.volume = effectivePlayerGain()
            updateState()
            return
        }
        applyFineGrainedVolume(requestedVolume)
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
        cancelPauseFade()
        shouldResumeAfterTransientFocusLoss = false
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            stopAndClearQueue()
        }
        updateState()
    }

    fun skipPrevious() {
        cancelPauseFade()
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
        cancelPauseFade()
        recordManualPlaybackStart()
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
        player.addMediaItem(song.toPlaybackMediaItem())
        _state.value = _state.value.copy(queue = existingQueue + song)
        updateState()
    }

    fun release() {
        pauseFadeJob?.cancel()
        progressUpdateJob?.cancel()
        usbDacHardwareVolumeManager.release()
        bitPerfectUsbManager.release()
        abandonAudioFocus()
        appContext.contentResolver.unregisterContentObserver(systemVolumeObserver)
        audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
        detachPlayerObservers(player)
        mediaSession.release()
        player.release()
    }

    private fun applyPreferredAudioDevice() {
        player.setPreferredAudioDevice(bitPerfectUsbManager.preferredOutputDevice())
    }

    private fun maybeRebuildPlayerForAudioPath() {
        if (isSwitchingAudioPath) return
        val shouldUseDirectPlayback = bitPerfectUsbManager.status.value.shouldUseDirectPlayback
        if (shouldUseDirectPlayback == isDirectPlaybackActive) {
            player.volume = targetPlayerOutputGain()
            return
        }
        switchPlayerAudioPath(useDirectPlayback = shouldUseDirectPlayback)
    }

    private fun switchPlayerAudioPath(useDirectPlayback: Boolean) {
        isSwitchingAudioPath = true
        try {
            val previousPlayer = player
            val playbackSnapshot = PlaybackSnapshot.from(previousPlayer)
            val queueSnapshot = _state.value.queue
            detachPlayerObservers(previousPlayer)

            val replacementPlayer = createPlayer(enableSignalProcessing = !useDirectPlayback)
            attachPlayerObservers(replacementPlayer)
            replacementPlayer.repeatMode = previousPlayer.repeatMode
            replacementPlayer.shuffleModeEnabled = previousPlayer.shuffleModeEnabled
            if (queueSnapshot.isNotEmpty()) {
                replacementPlayer.setMediaItems(
                    queueSnapshot.map(Song::toPlaybackMediaItem),
                    playbackSnapshot.currentIndex.coerceIn(0, queueSnapshot.lastIndex),
                    playbackSnapshot.positionMs,
                )
                replacementPlayer.prepare()
                replacementPlayer.playWhenReady = playbackSnapshot.playWhenReady
                if (playbackSnapshot.playWhenReady) {
                    replacementPlayer.play()
                }
            }
            player = replacementPlayer
            isDirectPlaybackActive = useDirectPlayback
            mediaSession.setPlayer(replacementPlayer)
            applyPreferredAudioDevice()
            replacementPlayer.volume = targetPlayerOutputGain()
            _playerInstanceVersion.value += 1L
            previousPlayer.release()
        } finally {
            isSwitchingAudioPath = false
        }
    }

    private fun setQueue(
        songs: List<Song>,
        startIndex: Int,
        sourceLabel: String?,
        shuffleEnabled: Boolean,
    ) {
        if (songs.isEmpty()) return
        cancelPauseFade()
        isManualPausePending = false
        isPauseTransitioningToStopped = false
        shouldResumeAfterTransientFocusLoss = false
        bitPerfectUsbManager.clearForStop()
        maybeRebuildPlayerForAudioPath()

        val mediaItems = songs.map { song ->
            song.toPlaybackMediaItem()
        }

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.shuffleModeEnabled = shuffleEnabled
        player.prepare()
        val shouldAutoPlay = requestAudioFocus()
        if (shouldAutoPlay) {
            player.volume = effectivePlayerGain()
            player.playWhenReady = true
            player.play()
        } else {
            player.playWhenReady = false
        }
        _state.value = _state.value.copy(
            queue = songs,
            currentIndex = startIndex.coerceIn(songs.indices),
            sourceLabel = sourceLabel,
            transportShowsPause = shouldAutoPlay,
        )
        updateState()
    }

    private fun stopAndClearQueue() {
        cancelPauseFade(resetVolume = false)
        isStoppingQueue = true
        shouldResumeAfterTransientFocusLoss = false
        bitPerfectUsbManager.clearForStop()
        player.pause()
        player.playWhenReady = false
        player.seekTo(0L)
        player.clearMediaItems()
        isManualPausePending = false
        isPauseTransitioningToStopped = false
        abandonAudioFocus()
        lastRecordedSongId = null
        _state.value = _state.value.copy(
            queue = emptyList(),
            currentIndex = -1,
            isPlaying = false,
            transportShowsPause = false,
            sourceLabel = null,
            audioSessionId = 0,
        )
        _progressState.value = playbackProgressController.clear()
        isStoppingQueue = false
    }

    private fun updateState() {
        val existingState = _state.value
        val currentIndex = resolveCurrentQueueIndex(existingState)
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
        userVolume = currentEffectiveVolumeFraction()

        val updatedState = existingState.copy(
            currentIndex = currentIndex,
            isPlaying = if (isPauseTransitioningToStopped) false else player.isPlaying,
            transportShowsPause = !isPauseTransitioningToStopped &&
                currentSong != null &&
                player.playWhenReady,
            repeatMode = player.repeatMode.toPlaybackRepeatMode(),
            shuffleEnabled = player.shuffleModeEnabled,
            sourceLabel = existingState.sourceLabel ?: currentSong?.album,
            volume = currentDisplayedVolumeFraction(),
            audioSessionId = player.audioSessionId.takeIf { it > 0 } ?: 0,
            recentSongIds = recentSongIds,
            recentAlbumIds = recentAlbumIds,
        )
        if (updatedState != existingState) {
            _state.value = updatedState
            if (
                updatedState.recentSongIds != existingState.recentSongIds ||
                updatedState.recentAlbumIds != existingState.recentAlbumIds
            ) {
                onRecentPlaybackChanged(
                    updatedState.recentSongIds,
                    updatedState.recentAlbumIds,
                )
            }
        }
        publishProgressSnapshot()
    }

    private fun publishProgressSnapshot() {
        val updatedProgress = playbackProgressController.onPlayerSnapshot(
            mediaId = currentSong()?.id,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0 }?.coerceAtLeast(0L) ?: 0L,
            bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
            isPlaying = if (isPauseTransitioningToStopped) false else player.isPlaying,
        )
        if (updatedProgress != _progressState.value) {
            _progressState.value = updatedProgress
        }
    }

    private fun resumePlayback() {
        cancelPauseFade()
        isManualPausePending = false
        bitPerfectUsbManager.updateEffectsActive(hasSignalAlteringEffects())
        bitPerfectUsbManager.refreshConnectedDevices()
        maybeRebuildPlayerForAudioPath()
        if (!requestAudioFocus()) return
        isPauseTransitioningToStopped = false
        shouldResumeAfterTransientFocusLoss = false
        player.volume = targetPlayerOutputGain()
        player.play()
    }

    private fun beginManualPauseFadeOut() {
        if (isDirectPlaybackActive) {
            player.pause()
            abandonAudioFocus()
            updateState()
            return
        }
        pauseFadeJob?.cancel()
        pauseFadeJob = scope.launch {
            val startVolume = player.volume.coerceIn(0f, 1f)
            if (startVolume > 0.001f) {
                repeat(PAUSE_FADE_STEP_COUNT) { step ->
                    if (!isActive) return@launch
                    val progress = (step + 1).toFloat() / PAUSE_FADE_STEP_COUNT.toFloat()
                    player.volume = lerp(
                        start = startVolume,
                        stop = 0f,
                        fraction = progress,
                    )
                    delay(PAUSE_FADE_STEP_DURATION_MS)
                }
            }
            player.pause()
            abandonAudioFocus()
            pauseFadeJob = null
            updateState()
        }
    }

    private fun cancelPauseFade(resetVolume: Boolean = true) {
        pauseFadeJob?.cancel()
        pauseFadeJob = null
        if (resetVolume) {
            player.volume = targetPlayerOutputGain()
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
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                if (shouldResumeAfterTransientFocusLoss && _state.value.queue.isNotEmpty()) {
                    resumePlayback()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                cancelPauseFade(resetVolume = false)
                shouldResumeAfterTransientFocusLoss = player.isPlaying || player.playWhenReady
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                player.pause()
                player.volume = effectivePlayerGain()
                updateState()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                cancelPauseFade(resetVolume = false)
                shouldResumeAfterTransientFocusLoss = player.isPlaying || player.playWhenReady
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                player.pause()
                player.volume = effectivePlayerGain()
                updateState()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                cancelPauseFade(resetVolume = false)
                shouldResumeAfterTransientFocusLoss = false
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                player.pause()
                player.volume = effectivePlayerGain()
                abandonAudioFocus()
                updateState()
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
        bitPerfectUsbManager.clearForStop()
        maybeRebuildPlayerForAudioPath()
        isRecoveringPlayback = true
        scope.launch {
            val mediaItems = snapshot.queue.map { song ->
                song.toPlaybackMediaItem()
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

    private fun currentSong(): Song? {
        val index = player.currentMediaItemIndex
        return _state.value.queue.getOrNull(index)
    }

    private fun recordManualPlaybackStart() {
        _manualPlaybackStartVersion.value = _manualPlaybackStartVersion.value + 1L
    }

    private fun effectivePlayerGain(): Float {
        if (isDirectPlaybackActive || usbDacHardwareVolumeManager.shouldBypassSoftwareVolume()) {
            return 1f
        }
        val baseGain = if (usesFixedVolumeOutput()) userVolume else volumeFineGain
        return baseGain.coerceIn(0f, 1f)
    }

    private fun lerp(
        start: Float,
        stop: Float,
        fraction: Float,
    ): Float = start + (stop - start) * fraction.coerceIn(0f, 1f)

    private fun applyFineGrainedVolume(targetVolume: Float) {
        if (usbDacHardwareVolumeManager.shouldBypassSoftwareVolume()) {
            userVolume = targetVolume
            player.volume = 1f
            return
        }
        if (isDirectPlaybackActive) {
            val manager = audioManager
            if (manager == null) {
                userVolume = targetVolume
                volumeFineGain = if (targetVolume <= 0f) 0f else 1f
                return
            }
            val maxStep = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val targetSystemStep = (targetVolume * maxStep.toFloat()).roundToInt().coerceIn(0, maxStep)
            ignoreObservedSystemVolumeStep = targetSystemStep
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, targetSystemStep, 0)
            volumeFineGain = if (targetSystemStep <= 0) 0f else 1f
            userVolume = currentSystemVolumeFraction().quantizedVolume()
            return
        }
        if (usesFixedVolumeOutput()) {
            userVolume = targetVolume
            volumeFineGain = targetVolume
            player.volume = targetPlayerOutputGain()
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
        if (pauseFadeJob?.isActive == true) {
            ignoreObservedSystemVolumeStep = null
            userVolume = currentEffectiveVolumeFraction()
            updateState()
            return
        }
        if (isDirectPlaybackActive || usbDacHardwareVolumeManager.shouldBypassSoftwareVolume()) {
            ignoreObservedSystemVolumeStep = null
            player.volume = if (isPauseTransitioningToStopped && !player.isPlaying) 0f else 1f
            userVolume = currentEffectiveVolumeFraction()
            updateState()
            return
        }
        if (usesFixedVolumeOutput()) {
            ignoreObservedSystemVolumeStep = null
            player.volume = targetPlayerOutputGain()
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
        player.volume = targetPlayerOutputGain()
        updateState()
    }

    private fun targetPlayerOutputGain(): Float {
        if (pauseFadeJob?.isActive == true) {
            return player.volume.coerceIn(0f, 1f)
        }
        return if (isPauseTransitioningToStopped && !player.isPlaying) {
            0f
        } else {
            effectivePlayerGain()
        }
    }

    private fun currentEffectiveVolumeFraction(): Float {
        if (usbDacHardwareVolumeManager.shouldOwnVolumeControls()) {
            return usbDacHardwareVolumeManager.currentHardwareVolume() ?: userVolume
        }
        if (isDirectPlaybackActive) {
            return currentSystemVolumeFraction().quantizedVolume()
        }
        if (shouldBypassSystemStreamVolume()) return userVolume
        val currentSystemFraction = currentSystemVolumeFraction()
        return (currentSystemFraction * volumeFineGain).coerceIn(0f, 1f).quantizedVolume()
    }

    private fun currentDisplayedVolumeFraction(): Float {
        return if (hasUsbOutputRoute) {
            currentSystemVolumeFraction().quantizedVolume()
        } else {
            currentEffectiveVolumeFraction()
        }
    }

    private fun shouldBypassSystemStreamVolume(): Boolean {
        return usbDacHardwareVolumeManager.shouldOwnVolumeControls() ||
            usesFixedVolumeOutput()
    }

    private fun usesFixedVolumeOutput(): Boolean {
        return audioManager?.isVolumeFixed == true
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

    private fun currentUsbOutputDescriptor(): UsbAudioDeviceDescriptor? {
        val manager = audioManager ?: return null
        return manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { device ->
                device.isSink && device.type in USB_AUDIO_OUTPUT_DEVICE_TYPES
            }
            ?.let { device ->
                UsbAudioDeviceDescriptor(
                    id = device.id,
                    type = device.type,
                    isSink = device.isSink,
                    productName = device.productName?.toString(),
                    sampleRates = device.sampleRates,
                    encodings = device.encodings,
                )
            }
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

    private fun resolveCurrentQueueIndex(existingState: PlaybackUiState): Int {
        val playerIndex = player.currentMediaItemIndex.takeIf { it >= 0 }
        if (playerIndex != null) return playerIndex

        val playerMediaId = player.currentMediaItem?.mediaId?.toLongOrNull()
        if (playerMediaId != null) {
            val matchedQueueIndex = existingState.queue.indexOfFirst { it.id == playerMediaId }
            if (matchedQueueIndex >= 0) return matchedQueueIndex
        }

        val fallbackIndex = existingState.currentIndex
        return fallbackIndex.takeIf { it in existingState.queue.indices } ?: -1
    }

    private companion object {
        const val PAUSE_FADE_DURATION_MS = 600L
        const val PAUSE_FADE_STEP_COUNT = 20
        const val PAUSE_FADE_STEP_DURATION_MS = PAUSE_FADE_DURATION_MS / PAUSE_FADE_STEP_COUNT
        const val PREVIOUS_SEEK_THRESHOLD_MS = 5_000L
        const val MAX_HISTORY_ITEMS = 12
    }
}

private fun Song.toPlaybackMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMimeType(inferPlaybackMimeType())
        .build()
}

private fun Song.inferPlaybackMimeType(): String? {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    val normalizedFormat = audioFormat.uppercase()
    return when {
        normalizedFormat == "ALAC" -> "audio/mp4"
        normalizedFormat == "AIFF" || extension in setOf("aif", "aiff", "aifc") -> "audio/aiff"
        extension in setOf("m4a", "mp4") -> "audio/mp4"
        extension == "aac" || normalizedFormat == "AAC" -> "audio/aac"
        extension == "flac" || normalizedFormat == "FLAC" -> "audio/flac"
        extension == "wav" || normalizedFormat == "WAV" -> "audio/wav"
        extension == "mp3" || normalizedFormat == "MP3" -> "audio/mpeg"
        extension == "ogg" || normalizedFormat == "OGG" -> "audio/ogg"
        extension == "opus" || normalizedFormat == "OPUS" -> "audio/opus"
        else -> null
    }
}

private fun Float.quantizedVolume(): Float {
    return ((coerceIn(0f, 1f) * 100f).roundToInt() / 100f).coerceIn(0f, 1f)
}

private fun Array<out AudioDeviceInfo>.hasUsbOutputDeviceChange(): Boolean {
    return any { device ->
        device.isSink && device.type in USB_AUDIO_OUTPUT_DEVICE_TYPES
    }
}

private data class PlaybackSnapshot(
    val currentIndex: Int,
    val positionMs: Long,
    val playWhenReady: Boolean,
) {
    companion object {
        fun from(player: Player): PlaybackSnapshot {
            return PlaybackSnapshot(
                currentIndex = player.currentMediaItemIndex.coerceAtLeast(0),
                positionMs = player.currentPosition.coerceAtLeast(0L),
                playWhenReady = player.playWhenReady,
            )
        }
    }
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

private val USB_AUDIO_OUTPUT_DEVICE_TYPES = setOf(
    AudioDeviceInfo.TYPE_USB_DEVICE,
    AudioDeviceInfo.TYPE_USB_HEADSET,
    AudioDeviceInfo.TYPE_USB_ACCESSORY,
)
