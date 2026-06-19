package elovaire.music.droidbeauty.app.data.playback

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
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.MediaSession
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.MainActivity
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

enum class PlaybackRepeatMode {
    Off,
    One,
    All,
}

enum class PlaybackCollectionKind {
    Album,
    Playlist,
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
    val sourcePlaylistId: Long? = null,
    val lastPlayedCollectionKind: PlaybackCollectionKind? = null,
    val lastPlayedCollectionId: Long? = null,
) {
    val currentSong: Song?
        get() = queue.getOrNull(currentIndex)
}

data class PlaybackNowPlayingState(
    val currentSong: Song? = null,
    val sourceLabel: String? = null,
    val audioSessionId: Int = 0,
)

data class PlaybackTransportState(
    val isPlaying: Boolean = false,
    val transportShowsPause: Boolean = false,
    val repeatMode: PlaybackRepeatMode = PlaybackRepeatMode.Off,
    val shuffleEnabled: Boolean = false,
)

data class PlaybackQueueState(
    val queue: List<Song> = emptyList(),
    val currentIndex: Int = -1,
    val sourcePlaylistId: Long? = null,
)

data class PlaybackVolumeState(
    val volume: Float = 1f,
)

data class RecentPlaybackState(
    val recentSongIds: List<Long> = emptyList(),
    val recentAlbumIds: List<Long> = emptyList(),
    val lastPlayedCollectionKind: PlaybackCollectionKind? = null,
    val lastPlayedCollectionId: Long? = null,
)

@SuppressLint("UnsafeOptInUsageError")
class PlaybackManager(
    context: Context,
    scope: CoroutineScope,
    audioProcessorsProvider: () -> Array<AudioProcessor> = { emptyArray() },
    hasSignalAlteringEffects: () -> Boolean = { false },
    initialRecentSongIds: List<Long> = emptyList(),
    initialRecentAlbumIds: List<Long> = emptyList(),
    initialLastPlayedCollectionKind: PlaybackCollectionKind? = null,
    initialLastPlayedCollectionId: Long? = null,
    onRecentPlaybackChanged: (
        songIds: List<Long>,
        albumIds: List<Long>,
        lastPlayedCollectionKind: PlaybackCollectionKind?,
        lastPlayedCollectionId: Long?,
    ) -> Unit = { _, _, _, _ -> },
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
        audioManager = audioManager,
        playbackAudioAttributes = platformPlaybackAudioAttributes,
    )
    private val extractorsFactory = DefaultExtractorsFactory()
        .setConstantBitrateSeekingEnabled(true)
    private val dataSourceFactory = DefaultDataSource.Factory(appContext)
    private val mediaSourceFactory = buildMediaSourceFactory()
    private val playbackHandler = Handler(Looper.getMainLooper())
    private var pendingAudioPathReason: String? = null
    private var isDirectPlaybackActive = false
    private var isSwitchingAudioPath = false
    private var lastAppliedPreferredDeviceKey: PreferredAudioDeviceKey? = null
    private var lastAppliedAudioPathDecisionKey: AudioPathDecisionKey? = null
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
                refreshUsbAudioOutputState()
                scheduleAudioPathReevaluation("audio-device-added", AUDIO_PATH_REEVALUATION_DELAY_MS)
            }
            syncFromObservedSystemVolume()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            if (removedDevices.hasUsbOutputDeviceChange()) {
                refreshUsbAudioOutputState()
                scheduleAudioPathReevaluation("audio-device-removed", AUDIO_PATH_REEVALUATION_DELAY_MS)
            }
            syncFromObservedSystemVolume()
        }
    }
    private var lastRecordedSongId: Long? = null
    private var hasAudioFocus = false
    private var isPauseTransitioningToStopped = false
    private var isManualPausePending = false
    private var shouldResumeAfterTransientFocusLoss = false
    private var pausedForAudioFocusLoss = false
    private var isStoppingQueue = false
    private var isRecoveringPlayback = false
    private var lastKnownQueueIndex = -1
    private var lastKnownPositionMs = 0L
    private var unexpectedIdleRecoveryCount = 0
    private var lastUnexpectedIdleRecoveryElapsedMs = 0L
    private var hasUsbOutputRoute = false
    private val playbackProgressController = PlaybackProgressController()
    private var progressUpdateJob: Job? = null
    private var pauseFadeJob: Job? = null
    private val _playerInstanceVersion = MutableStateFlow(0L)
    val playerInstanceVersion: StateFlow<Long> = _playerInstanceVersion.asStateFlow()
    private val audioPathReevaluationRunnable = Runnable {
        val reason = pendingAudioPathReason ?: "unspecified"
        pendingAudioPathReason = null
        applyPreferredAudioDeviceIfNeeded()
        maybeRebuildPlayerForAudioPath(reason)
    }
    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(
            mediaItem: MediaItem?,
            reason: Int,
        ) {
            resetUnexpectedIdleRecoveryGuard()
            scheduleAudioPathReevaluation("media-item-transition", AUDIO_PATH_REEVALUATION_DELAY_MS)
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
                recoverUnexpectedIdleState(shouldAutoPlay = shouldAutoResumeAfterUnexpectedIdle())
            } else {
                if (playbackState != Player.STATE_IDLE) {
                    resetUnexpectedIdleRecoveryGuard()
                }
                updateState()
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            if (_state.value.queue.isNotEmpty() && !isStoppingQueue && !isRecoveringPlayback) {
                recoverUnexpectedIdleState(shouldAutoPlay = shouldAutoResumeAfterUnexpectedIdle())
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
            scheduleAudioPathReevaluation("audio-track-initialized")
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
            lastPlayedCollectionKind = initialLastPlayedCollectionKind,
            lastPlayedCollectionId = initialLastPlayedCollectionId,
        ),
    )
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()
    val nowPlayingState: StateFlow<PlaybackNowPlayingState> = state
        .map { snapshot ->
            PlaybackNowPlayingState(
                currentSong = snapshot.currentSong,
                sourceLabel = snapshot.sourceLabel,
                audioSessionId = snapshot.audioSessionId,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = PlaybackNowPlayingState(
                currentSong = _state.value.currentSong,
                sourceLabel = _state.value.sourceLabel,
                audioSessionId = _state.value.audioSessionId,
            ),
        )
    val transportState: StateFlow<PlaybackTransportState> = state
        .map { snapshot ->
            PlaybackTransportState(
                isPlaying = snapshot.isPlaying,
                transportShowsPause = snapshot.transportShowsPause,
                repeatMode = snapshot.repeatMode,
                shuffleEnabled = snapshot.shuffleEnabled,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = PlaybackTransportState(
                isPlaying = _state.value.isPlaying,
                transportShowsPause = _state.value.transportShowsPause,
                repeatMode = _state.value.repeatMode,
                shuffleEnabled = _state.value.shuffleEnabled,
            ),
        )
    val queueState: StateFlow<PlaybackQueueState> = state
        .map { snapshot ->
            PlaybackQueueState(
                queue = snapshot.queue,
                currentIndex = snapshot.currentIndex,
                sourcePlaylistId = snapshot.sourcePlaylistId,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = PlaybackQueueState(
                queue = _state.value.queue,
                currentIndex = _state.value.currentIndex,
                sourcePlaylistId = _state.value.sourcePlaylistId,
            ),
        )
    val volumeState: StateFlow<PlaybackVolumeState> = state
        .map { snapshot -> PlaybackVolumeState(volume = snapshot.volume) }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = PlaybackVolumeState(volume = _state.value.volume),
        )
    val recentPlaybackState: StateFlow<RecentPlaybackState> = state
        .map { snapshot ->
            RecentPlaybackState(
                recentSongIds = snapshot.recentSongIds,
                recentAlbumIds = snapshot.recentAlbumIds,
                lastPlayedCollectionKind = snapshot.lastPlayedCollectionKind,
                lastPlayedCollectionId = snapshot.lastPlayedCollectionId,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = RecentPlaybackState(
                recentSongIds = _state.value.recentSongIds,
                recentAlbumIds = _state.value.recentAlbumIds,
                lastPlayedCollectionKind = _state.value.lastPlayedCollectionKind,
                lastPlayedCollectionId = _state.value.lastPlayedCollectionId,
            ),
        )
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
        bitPerfectUsbManager.updateEffectsActive(hasSignalAlteringEffects())
        refreshUsbAudioOutputState()
        appContext.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            systemVolumeObserver,
            )
        audioManager?.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        applyPreferredAudioDeviceIfNeeded(force = true)
        attachPlayerObservers(player)
        syncFromObservedSystemVolume()
        player.volume = effectivePlayerGain()

        syncProgressUpdateLoop()
    }

    fun reevaluateAudioOutputPath() {
        bitPerfectUsbManager.updateEffectsActive(hasSignalAlteringEffects())
        refreshUsbAudioOutputState()
        scheduleAudioPathReevaluation("effects-updated", AUDIO_PATH_REEVALUATION_DELAY_MS)
        player.volume = targetPlayerOutputGain()
        updateState()
    }

    fun playSong(
        song: Song,
        collection: List<Song>,
        sourceLabel: String? = song.album,
        shuffleEnabled: Boolean = false,
        sourcePlaylistId: Long? = null,
    ) {
        recordManualPlaybackStart()
        val startIndex = collection.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        setQueue(collection, startIndex, sourceLabel, shuffleEnabled, sourcePlaylistId)
    }

    private fun createPlayer(enableSignalProcessing: Boolean): ExoPlayer {
        val configuredPlayer = ExoPlayer.Builder(appContext)
            .setRenderersFactory(
                ElovaireRenderersFactory(
                    appContext,
                    if (enableSignalProcessing) audioProcessorsProvider() else emptyArray(),
                )
                    // Keep the framework sink configuration stable across the regular and direct
                    // paths so direct-playback eligibility doesn't oscillate between players.
                    .setEnableAudioFloatOutput(false)
                    .setEnableDecoderFallback(true)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER),
            )
            .setMediaSourceFactory(
                mediaSourceFactory,
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
        sourcePlaylistId: Long? = null,
    ) {
        recordManualPlaybackStart()
        val startIndex = if (startSongId == null) {
            0
        } else {
            album.songs.indexOfFirst { it.id == startSongId }.coerceAtLeast(0)
        }
        setQueue(album.songs, startIndex, sourceLabel, shuffleEnabled, sourcePlaylistId)
    }

    fun togglePlayback() {
        shouldResumeAfterTransientFocusLoss = false
        pausedForAudioFocusLoss = false
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
        syncProgressUpdateLoop()
    }

    fun updateScrubPosition(positionMs: Long) {
        _progressState.value = playbackProgressController.updateScrubPosition(positionMs)
    }

    fun finishScrub(positionMs: Long) {
        val result = playbackProgressController.finishScrub(positionMs)
        _progressState.value = result.state
        result.seekPositionMs?.let(player::seekTo)
        syncProgressUpdateLoop()
    }

    fun cancelScrub() {
        _progressState.value = playbackProgressController.cancelScrub()
        syncProgressUpdateLoop()
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
        pausedForAudioFocusLoss = false
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
        pausedForAudioFocusLoss = false
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
        pausedForAudioFocusLoss = false
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

    fun removeQueueIndex(index: Int) {
        val existingQueue = _state.value.queue
        if (index !in existingQueue.indices) return
        if (existingQueue.size == 1) {
            stopAndClearQueue()
            return
        }
        cancelPauseFade()
        shouldResumeAfterTransientFocusLoss = false
        pausedForAudioFocusLoss = false
        val currentIndex = resolveCurrentQueueIndex(_state.value).takeIf { it in existingQueue.indices } ?: _state.value.currentIndex
        val shouldKeepPlaying = _state.value.transportShowsPause || player.isPlaying || player.playWhenReady
        val updatedQueue = existingQueue.toMutableList().apply { removeAt(index) }
        player.removeMediaItem(index)
        val fallbackIndex = when {
            index < currentIndex -> currentIndex - 1
            currentIndex >= updatedQueue.size -> updatedQueue.lastIndex
            else -> currentIndex
        }.coerceIn(0, updatedQueue.lastIndex)
        _state.value = _state.value.copy(
            queue = updatedQueue,
            currentIndex = fallbackIndex,
            transportShowsPause = shouldKeepPlaying,
        )
        if (shouldKeepPlaying && requestAudioFocus()) {
            player.volume = effectivePlayerGain()
            player.playWhenReady = true
            if (!player.isPlaying) {
                player.play()
            }
        }
        updateState()
    }

    fun refreshLibraryMetadata(updatedSongs: List<Song>) {
        val existingState = _state.value
        if (existingState.queue.isEmpty()) return
        val songsById = updatedSongs.associateBy(Song::id)
        var queueChanged = false
        val refreshedQueue = existingState.queue.map { queuedSong ->
            val refreshedSong = songsById[queuedSong.id]
            if (refreshedSong != null && refreshedSong != queuedSong) {
                queueChanged = true
                refreshedSong
            } else {
                queuedSong
            }
        }
        if (!queueChanged) return
        val currentIndex = resolveCurrentQueueIndex(existingState)
        val previousCurrentSong = existingState.queue.getOrNull(currentIndex)
        val refreshedCurrentSong = refreshedQueue.getOrNull(currentIndex)
        val refreshedSourceLabel = when {
            existingState.sourcePlaylistId != null -> existingState.sourceLabel
            existingState.sourceLabel == previousCurrentSong?.album -> refreshedCurrentSong?.album
            else -> existingState.sourceLabel
        }
        _state.value = existingState.copy(
            queue = refreshedQueue,
            sourceLabel = refreshedSourceLabel,
        )
        updateState()
    }

    fun release() {
        pauseFadeJob?.cancel()
        progressUpdateJob?.cancel()
        playbackHandler.removeCallbacks(audioPathReevaluationRunnable)
        usbDacHardwareVolumeManager.release()
        abandonAudioFocus()
        appContext.contentResolver.unregisterContentObserver(systemVolumeObserver)
        audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
        detachPlayerObservers(player)
        mediaSession.release()
        player.release()
    }

    private fun scheduleAudioPathReevaluation(
        reason: String,
        delayMs: Long = 0L,
    ) {
        pendingAudioPathReason = pendingAudioPathReason
            ?.takeIf { it == reason }
            ?: reason
        playbackHandler.removeCallbacks(audioPathReevaluationRunnable)
        playbackHandler.postDelayed(audioPathReevaluationRunnable, delayMs.coerceAtLeast(0L))
    }

    private fun refreshUsbAudioOutputState() {
        val currentUsbOutput = currentUsbOutputDescriptor()
        hasUsbOutputRoute = currentUsbOutput != null
        usbDacHardwareVolumeManager.updateAudioOutputDevice(currentUsbOutput)
        bitPerfectUsbManager.refreshConnectedDevices()
    }

    private fun applyPreferredAudioDeviceIfNeeded(force: Boolean = false) {
        val preferredDevice = bitPerfectUsbManager.preferredOutputDevice()
        val nextKey = preferredDevice?.let { PreferredAudioDeviceKey(it.id, it.type) }
        if (!force && lastAppliedPreferredDeviceKey == nextKey) return
        player.setPreferredAudioDevice(preferredDevice)
        lastAppliedPreferredDeviceKey = nextKey
    }

    private fun maybeRebuildPlayerForAudioPath(reason: String) {
        if (isSwitchingAudioPath) return
        if (_state.value.queue.isEmpty() && player.mediaItemCount == 0) {
            lastAppliedAudioPathDecisionKey = null
            player.volume = targetPlayerOutputGain()
            return
        }
        val status = bitPerfectUsbManager.status.value
        val desiredUseDirectPlayback = when (status.directive) {
            BitPerfectPlaybackDirective.KeepCurrent -> isDirectPlaybackActive
            BitPerfectPlaybackDirective.PreferDirect -> true
            BitPerfectPlaybackDirective.PreferRegular -> false
        }
        val nextDecisionKey = AudioPathDecisionKey(
            useDirectPlayback = desiredUseDirectPlayback,
            directive = status.directive,
            evaluationKey = status.evaluationKey,
            routeDeviceId = status.activeRouteDeviceId,
            routeType = status.activeRouteType,
            preferredDeviceKey = bitPerfectUsbManager.preferredOutputDevice()
                ?.let { PreferredAudioDeviceKey(it.id, it.type) },
        )
        if (nextDecisionKey == lastAppliedAudioPathDecisionKey) {
            player.volume = targetPlayerOutputGain()
            return
        }
        when (status.directive) {
            BitPerfectPlaybackDirective.KeepCurrent -> {
                lastAppliedAudioPathDecisionKey = nextDecisionKey
                player.volume = targetPlayerOutputGain()
            }

            BitPerfectPlaybackDirective.PreferDirect -> {
                if (!isDirectPlaybackActive) {
                    switchPlayerAudioPath(
                        useDirectPlayback = true,
                        reason = reason,
                        decisionKey = nextDecisionKey,
                    )
                } else {
                    lastAppliedAudioPathDecisionKey = nextDecisionKey
                    player.volume = targetPlayerOutputGain()
                }
            }

            BitPerfectPlaybackDirective.PreferRegular -> {
                if (isDirectPlaybackActive) {
                    switchPlayerAudioPath(
                        useDirectPlayback = false,
                        reason = reason,
                        decisionKey = nextDecisionKey,
                    )
                } else {
                    lastAppliedAudioPathDecisionKey = nextDecisionKey
                    player.volume = targetPlayerOutputGain()
                }
            }
        }
    }

    private fun switchPlayerAudioPath(
        useDirectPlayback: Boolean,
        reason: String,
        decisionKey: AudioPathDecisionKey,
    ) {
        isSwitchingAudioPath = true
        try {
            val previousPlayer = player
            val playbackSnapshot = PlaybackSnapshot.from(previousPlayer)
            val queueSnapshot = _state.value.queue
            detachPlayerObservers(previousPlayer)
            logDebug("rebuild direct=$useDirectPlayback reason=$reason position=${playbackSnapshot.positionMs} index=${playbackSnapshot.currentIndex}")

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
            lastAppliedPreferredDeviceKey = null
            applyPreferredAudioDeviceIfNeeded(force = true)
            lastAppliedAudioPathDecisionKey = decisionKey
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
        sourcePlaylistId: Long?,
    ) {
        if (songs.isEmpty()) return
        cancelPauseFade()
        isManualPausePending = false
        isPauseTransitioningToStopped = false
        shouldResumeAfterTransientFocusLoss = false
        pausedForAudioFocusLoss = false
        bitPerfectUsbManager.clearPlaybackFormat()
        lastAppliedAudioPathDecisionKey = null
        scheduleAudioPathReevaluation("set-queue", AUDIO_PATH_REEVALUATION_DELAY_MS)
        resetUnexpectedIdleRecoveryGuard()

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
            sourcePlaylistId = sourcePlaylistId,
        )
        updateState()
    }

    private fun stopAndClearQueue() {
        cancelPauseFade(resetVolume = false)
        isStoppingQueue = true
        shouldResumeAfterTransientFocusLoss = false
        pausedForAudioFocusLoss = false
        bitPerfectUsbManager.clearForStop()
        lastAppliedAudioPathDecisionKey = null
        playbackHandler.removeCallbacks(audioPathReevaluationRunnable)
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
            sourcePlaylistId = null,
        )
        _progressState.value = playbackProgressController.clear()
        syncProgressUpdateLoop()
        resetUnexpectedIdleRecoveryGuard()
        isStoppingQueue = false
    }

    private fun updateState() {
        val existingState = _state.value
        val currentIndex = resolveCurrentQueueIndex(existingState)
        val currentSong = existingState.queue.getOrNull(currentIndex)
        if (currentSong != null && (player.isPlaying || player.playWhenReady)) {
            resetUnexpectedIdleRecoveryGuard()
        }
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
        val lastPlayedCollectionKind = if (hasNewSong) {
            if (existingState.sourcePlaylistId != null) {
                PlaybackCollectionKind.Playlist
            } else {
                PlaybackCollectionKind.Album
            }
        } else {
            existingState.lastPlayedCollectionKind
        }
        val lastPlayedCollectionId = if (hasNewSong) {
            existingState.sourcePlaylistId ?: currentSong.albumId
        } else {
            existingState.lastPlayedCollectionId
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
            lastPlayedCollectionKind = lastPlayedCollectionKind,
            lastPlayedCollectionId = lastPlayedCollectionId,
        )
        if (updatedState != existingState) {
            _state.value = updatedState
            if (
                updatedState.recentSongIds != existingState.recentSongIds ||
                updatedState.recentAlbumIds != existingState.recentAlbumIds ||
                updatedState.lastPlayedCollectionKind != existingState.lastPlayedCollectionKind ||
                updatedState.lastPlayedCollectionId != existingState.lastPlayedCollectionId
            ) {
                onRecentPlaybackChanged(
                    updatedState.recentSongIds,
                    updatedState.recentAlbumIds,
                    updatedState.lastPlayedCollectionKind,
                    updatedState.lastPlayedCollectionId,
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
        syncProgressUpdateLoop()
    }

    private fun resumePlayback() {
        cancelPauseFade()
        isManualPausePending = false
        pausedForAudioFocusLoss = false
        bitPerfectUsbManager.updateEffectsActive(hasSignalAlteringEffects())
        refreshUsbAudioOutputState()
        scheduleAudioPathReevaluation("resume-playback")
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
                val shouldResume = pausedForAudioFocusLoss &&
                    shouldResumeAfterTransientFocusLoss &&
                    _state.value.queue.isNotEmpty() &&
                    !isManualPausePending
                if (shouldResume) {
                    resumePlayback()
                } else {
                    pausedForAudioFocusLoss = false
                    player.volume = effectivePlayerGain()
                    updateState()
                }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                cancelPauseFade(resetVolume = false)
                shouldResumeAfterTransientFocusLoss =
                    (player.isPlaying || player.playWhenReady || _state.value.transportShowsPause) &&
                        _state.value.queue.isNotEmpty()
                pausedForAudioFocusLoss = shouldResumeAfterTransientFocusLoss
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                player.pause()
                player.volume = effectivePlayerGain()
                updateState()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                cancelPauseFade(resetVolume = false)
                shouldResumeAfterTransientFocusLoss =
                    (player.isPlaying || player.playWhenReady || _state.value.transportShowsPause) &&
                        _state.value.queue.isNotEmpty()
                pausedForAudioFocusLoss = shouldResumeAfterTransientFocusLoss
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                player.pause()
                player.volume = effectivePlayerGain()
                updateState()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                cancelPauseFade(resetVolume = false)
                shouldResumeAfterTransientFocusLoss = false
                pausedForAudioFocusLoss = false
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
        if (!registerUnexpectedIdleRecoveryAttempt()) {
            enterSafeStoppedStateAfterRecoveryFailure(snapshot)
            return
        }
        val recoverIndex = lastKnownQueueIndex
            .takeIf { it in snapshot.queue.indices }
            ?: snapshot.currentIndex.takeIf { it in snapshot.queue.indices }
            ?: 0
        val recoverPosition = lastKnownPositionMs.coerceAtLeast(0L)
        bitPerfectUsbManager.clearPlaybackFormat()
        lastAppliedAudioPathDecisionKey = null
        scheduleAudioPathReevaluation("recover-idle", AUDIO_PATH_REEVALUATION_DELAY_MS)
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

    private fun registerUnexpectedIdleRecoveryAttempt(): Boolean {
        val nowElapsedMs = SystemClock.elapsedRealtime()
        unexpectedIdleRecoveryCount = if (
            nowElapsedMs - lastUnexpectedIdleRecoveryElapsedMs <= UNEXPECTED_IDLE_RECOVERY_WINDOW_MS
        ) {
            unexpectedIdleRecoveryCount + 1
        } else {
            1
        }
        lastUnexpectedIdleRecoveryElapsedMs = nowElapsedMs
        return unexpectedIdleRecoveryCount <= MAX_UNEXPECTED_IDLE_RECOVERY_ATTEMPTS
    }

    private fun resetUnexpectedIdleRecoveryGuard() {
        unexpectedIdleRecoveryCount = 0
        lastUnexpectedIdleRecoveryElapsedMs = 0L
    }

    private fun enterSafeStoppedStateAfterRecoveryFailure(snapshot: PlaybackUiState) {
        logDebug("recovery aborted after repeated idle/player errors")
        cancelPauseFade(resetVolume = false)
        shouldResumeAfterTransientFocusLoss = false
        pausedForAudioFocusLoss = false
        isManualPausePending = false
        isPauseTransitioningToStopped = false
        isRecoveringPlayback = false
        player.pause()
        player.playWhenReady = false
        abandonAudioFocus()
        val nextState = snapshot.copy(
            isPlaying = false,
            transportShowsPause = false,
            audioSessionId = player.audioSessionId.takeIf { it > 0 } ?: 0,
        )
        if (nextState != _state.value) {
            _state.value = nextState
        }
        publishProgressSnapshot()
    }

    private fun currentSong(): Song? {
        val index = player.currentMediaItemIndex
        return _state.value.queue.getOrNull(index)
    }

    private fun syncProgressUpdateLoop() {
        val shouldPoll = player.isPlaying || playbackProgressController.needsActivePolling()
        if (!shouldPoll) {
            progressUpdateJob?.cancel()
            progressUpdateJob = null
            return
        }
        if (progressUpdateJob?.isActive == true) return
        progressUpdateJob = scope.launch {
            try {
                while (isActive) {
                    publishProgressSnapshot()
                    if (!player.isPlaying && !playbackProgressController.needsActivePolling()) {
                        break
                    }
                    delay(PLAYING_PROGRESS_UPDATE_INTERVAL_MS)
                }
            } finally {
                if (progressUpdateJob === this) {
                    progressUpdateJob = null
                }
            }
        }
    }

    private fun shouldAutoResumeAfterUnexpectedIdle(): Boolean {
        val snapshot = _state.value
        return snapshot.queue.isNotEmpty() &&
            !isManualPausePending &&
            (snapshot.transportShowsPause || snapshot.isPlaying || player.playWhenReady || shouldResumeAfterTransientFocusLoss)
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
        val routedUsbDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            manager.getAudioDevicesForAttributes(platformPlaybackAudioAttributes)
                .firstOrNull { device ->
                    device.isSink && device.type in USB_AUDIO_OUTPUT_DEVICE_TYPES
                }
        } else {
            null
        }
        return (routedUsbDevice ?: manager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .firstOrNull { device ->
                device.isSink && device.type in USB_AUDIO_OUTPUT_DEVICE_TYPES
            })
            ?.toUsbAudioDeviceDescriptor()
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
        const val PLAYING_PROGRESS_UPDATE_INTERVAL_MS = 250L
        const val AUDIO_PATH_REEVALUATION_DELAY_MS = 80L
        const val MAX_UNEXPECTED_IDLE_RECOVERY_ATTEMPTS = 3
        const val UNEXPECTED_IDLE_RECOVERY_WINDOW_MS = 10_000L
        const val TAG = "PlaybackManager"
    }

    private fun buildMediaSourceFactory(): MediaSource.Factory {
        return DefaultMediaSourceFactory(dataSourceFactory, extractorsFactory)
    }

    private fun logDebug(message: String) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, message)
    }
}

private data class AudioPathDecisionKey(
    val useDirectPlayback: Boolean,
    val directive: BitPerfectPlaybackDirective,
    val evaluationKey: DirectPlaybackEvaluationKey?,
    val routeDeviceId: Int?,
    val routeType: Int?,
    val preferredDeviceKey: PreferredAudioDeviceKey?,
)

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

private data class PreferredAudioDeviceKey(
    val id: Int,
    val type: Int,
)

private fun AudioDeviceInfo.toUsbAudioDeviceDescriptor(): UsbAudioDeviceDescriptor {
    return UsbAudioDeviceDescriptor(
        id = id,
        type = type,
        isSink = isSink,
        productName = productName?.toString(),
        sampleRates = sampleRates.copyOf(),
        encodings = encodings.copyOf(),
    )
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
