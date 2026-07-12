package elovaire.music.droidbeauty.app.data.playback

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.core.content.ContextCompat
import elovaire.music.droidbeauty.app.BuildConfig
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace
import elovaire.music.droidbeauty.app.core.safeOutputDevices
import elovaire.music.droidbeauty.app.core.safeRoutedOutputDevicesForAttributes
import elovaire.music.droidbeauty.app.core.supportsVerifiedDirectPlaybackRouting
import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy
import elovaire.music.droidbeauty.app.data.audio.PlaybackFailureClassifier
import elovaire.music.droidbeauty.app.domain.model.Album
import elovaire.music.droidbeauty.app.domain.model.Song
import elovaire.music.droidbeauty.app.domain.model.VolumeNormalizationPolicy
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
import java.util.concurrent.atomic.AtomicBoolean
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

enum class PlaybackCommand {
    Play,
    Pause,
    Toggle,
}

enum class PlaybackCommandOrigin {
    InApp,
    ExternalController,
    AudioInterruption,
    BecomingNoisy,
}

private enum class InterruptionResumeReason {
    None,
    TransientFocusLoss,
    PermanentLossWithActiveExternalMedia,
}

private data class InterruptionResumeState(
    val shouldResume: Boolean = false,
    val reason: InterruptionResumeReason = InterruptionResumeReason.None,
    val startedAtElapsedMs: Long = 0L,
    val resumeAttempts: Int = 0,
) {
    val isActive: Boolean get() = shouldResume
}

private enum class PauseFadeReason {
    Manual,
    AudioInterruption,
    BecomingNoisy,
}

private enum class ResumeFadeReason {
    Manual,
    AudioInterruption,
    Recovery,
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

data class PlaybackFormatFailure(
    val songId: Long,
    val fileName: String,
    val format: String,
    val reason: String,
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
) : PlaybackReader, PlaybackController {
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
    private val playerFactory = PlaybackPlayerFactory(
        context = appContext,
        dataSourceFactory = dataSourceFactory,
        extractorsFactory = extractorsFactory,
        playbackAudioAttributes = playbackAudioAttributes,
        audioProcessorsProvider = audioProcessorsProvider,
        preferredOutputDevice = bitPerfectUsbManager::preferredOutputDevice,
        offloadPolicyProvider = ::currentOffloadPolicy,
    )
    private val playbackHandler = Handler(Looper.getMainLooper())
    private var pendingAudioPathReason: String? = null
    private var isDirectPlaybackActive = false
    private var runtimeTransition: PlaybackRuntimeTransition = PlaybackRuntimeTransition.Idle
    private var lastAppliedPreferredDeviceKey: PreferredAudioDeviceKey? = null
    private var lastAppliedAudioPathDecisionKey: AudioPathDecisionKey? = null
    private var gaplessPlaybackEnabled = false
    private var volumeNormalizationEnabled = false
    private var volumeObserverRegistered = false
    private var audioDeviceCallbackRegistered = false
    private var noisyReceiverRegistered = false
    private var player = createPlayer(enableSignalProcessing = true)
    private val sleepTimerController = PlaybackSleepTimerController(
        scope = scope,
        elapsedRealtimeMs = SystemClock::elapsedRealtime,
        onTimerFired = ::stopAndClearQueue,
        setPauseAtEndOfMediaItems = { enabled -> player.setPauseAtEndOfMediaItems(enabled) },
    )
    private var commandGatewayPlayer: Player = PlaybackExternalCommandGateway(player, ::dispatchPlaybackCommand)
    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            platformPlaybackAudioAttributes,
        )
        .setOnAudioFocusChangeListener(::handleAudioFocusChange)
        .setAcceptsDelayedFocusGain(false)
        .setWillPauseWhenDucked(true)
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
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                dispatchPlaybackCommand(PlaybackCommand.Pause, PlaybackCommandOrigin.BecomingNoisy)
            }
        }
    }
    private var hasAudioFocus = false
    private var isPauseTransitioningToStopped = false
    private var isManualPausePending = false
    private var shouldResumeAfterTransientFocusLoss = false
    private var pausedForAudioFocusLoss = false
    private var pendingResumeAfterExternalInterruption = false
    private var interruptionResumeState = InterruptionResumeState()
    private var isStoppingQueue = false
    private val released = AtomicBoolean(false)
    private val failedPlaybackSongIds = mutableSetOf<Long>()
    private var unexpectedIdleRecoveryCount = 0
    private var lastUnexpectedIdleRecoveryElapsedMs = 0L
    private var hasUsbOutputRoute = false
    private val playbackProgressController = PlaybackProgressController()
    private val progressDemandController = PlaybackProgressDemandController()
    private val playbackProgressTicker = PlaybackProgressTicker(
        scope = scope,
        intervalMs = progressDemandController::pollingIntervalMs,
    ) {
        publishProgressSnapshot()
        shouldPollProgress()
    }
    private val queueMetadataRefresher = PlaybackQueueMetadataRefresher()
    private val stateReducer = PlaybackStateReducer(
        playerProvider = { player },
        currentDisplayedVolume = ::currentDisplayedVolumeFraction,
        onRecentPlaybackChanged = onRecentPlaybackChanged,
    )
    private var pauseFadeJob: Job? = null
    private var externalInterruptionResumeJob: Job? = null
    private var pendingAutoResumeRetryJob: Job? = null
    private var statePublishScheduled = false
    private val _playerInstanceVersion = MutableStateFlow(0L)
    val playerInstanceVersion: StateFlow<Long> = _playerInstanceVersion.asStateFlow()
    private val uiSharing = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L)
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
            bitPerfectUsbManager.updateEffectsActive(hasActiveSignalAlteringEffects())
            scheduleAudioPathReevaluation("media-item-transition", audioPathReevaluationDelayForTransition())
            sleepTimerController.updateEndOfSongTarget(currentSong()?.id)
            player.volume = targetPlayerOutputGain()
            scheduleStatePublish()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
        ) {
            sleepTimerController.updateEndOfSongTarget(currentSong()?.id)
            scheduleStatePublish()
        }

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            reason: Int,
        ) {
            if (!playWhenReady && sleepTimerState.value.option == SleepTimerOption.EndOfSong && isPausedAtEndOfCurrentSong()) {
                sleepTimerController.onEndOfSongReached()
            } else {
                scheduleStatePublish()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (sleepTimerState.value.option == SleepTimerOption.EndOfSong && isPausedAtEndOfCurrentSong()) {
                sleepTimerController.onEndOfSongReached()
            } else if (playbackState == Player.STATE_ENDED && player.repeatMode == Player.REPEAT_MODE_OFF) {
                stopAndClearQueue()
            } else if (
                playbackState == Player.STATE_IDLE &&
                !isStoppingQueue &&
                _state.value.queue.isNotEmpty() &&
                runtimeTransition !is PlaybackRuntimeTransition.Recovering
            ) {
                recoverUnexpectedIdleState(shouldAutoPlay = shouldAutoResumeAfterUnexpectedIdle())
            } else {
                if (playbackState != Player.STATE_IDLE) {
                    resetUnexpectedIdleRecoveryGuard()
                }
                scheduleStatePublish()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            if (error.isUnsupportedFormatError() && handleUnsupportedPlaybackFormat(error)) {
                return
            }
            if (_state.value.queue.isNotEmpty() && !isStoppingQueue && runtimeTransition !is PlaybackRuntimeTransition.Recovering) {
                recoverUnexpectedIdleState(shouldAutoPlay = shouldAutoResumeAfterUnexpectedIdle())
            } else {
                scheduleStatePublish()
            }
        }

        override fun onEvents(player: Player, events: Player.Events) {
            scheduleStatePublish()
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
            scheduleStatePublish()
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
    override val nowPlayingState: StateFlow<PlaybackNowPlayingState> = state
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
            started = uiSharing,
            initialValue = PlaybackNowPlayingState(
                currentSong = _state.value.currentSong,
                sourceLabel = _state.value.sourceLabel,
                audioSessionId = _state.value.audioSessionId,
            ),
        )
    override val transportState: StateFlow<PlaybackTransportState> = state
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
            started = uiSharing,
            initialValue = PlaybackTransportState(
                isPlaying = _state.value.isPlaying,
                transportShowsPause = _state.value.transportShowsPause,
                repeatMode = _state.value.repeatMode,
                shuffleEnabled = _state.value.shuffleEnabled,
            ),
        )
    override val queueState: StateFlow<PlaybackQueueState> = state
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
            started = uiSharing,
            initialValue = PlaybackQueueState(
                queue = _state.value.queue,
                currentIndex = _state.value.currentIndex,
                sourcePlaylistId = _state.value.sourcePlaylistId,
            ),
        )
    override val volumeState: StateFlow<PlaybackVolumeState> = state
        .map { snapshot -> PlaybackVolumeState(volume = snapshot.volume) }
        .distinctUntilChanged()
        .stateIn(
            scope = scope,
            started = uiSharing,
            initialValue = PlaybackVolumeState(volume = _state.value.volume),
        )
    override val recentPlaybackState: StateFlow<RecentPlaybackState> = state
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
            started = uiSharing,
            initialValue = RecentPlaybackState(
                recentSongIds = _state.value.recentSongIds,
                recentAlbumIds = _state.value.recentAlbumIds,
                lastPlayedCollectionKind = _state.value.lastPlayedCollectionKind,
                lastPlayedCollectionId = _state.value.lastPlayedCollectionId,
            ),
        )
    private val _progressState = MutableStateFlow(PlaybackProgressState())
    val progressState: StateFlow<PlaybackProgressState> = _progressState.asStateFlow()
    private val _playbackFormatFailure = MutableStateFlow<PlaybackFormatFailure?>(null)
    val playbackFormatFailure: StateFlow<PlaybackFormatFailure?> = _playbackFormatFailure.asStateFlow()
    private val _manualPlaybackStartVersion = MutableStateFlow(0L)
    val manualPlaybackStartVersion: StateFlow<Long> = _manualPlaybackStartVersion.asStateFlow()
    val sleepTimerState: StateFlow<PlaybackSleepTimerState> = sleepTimerController.state
    val playerInstance: Player
        get() = commandGatewayPlayer
    val mediaSessionToken
        get() = sessionOwner.mediaSessionToken
    val platformMediaSessionToken
        get() = sessionOwner.platformMediaSessionToken
    internal val mediaLibrarySession: MediaLibrarySession
        get() = sessionOwner.mediaLibrarySession
    private val sessionOwner = PlaybackSessionOwner(context, commandGatewayPlayer)
    internal fun setMediaLibrarySessionCallback(callback: MediaLibrarySession.Callback) {
        sessionOwner.setMediaLibrarySessionCallback(callback)
    }

    private val playerSwitcher = PlaybackPlayerSwitcher(
        createPlayer = ::createPlayer,
        attachPlayerObservers = ::attachPlayerObservers,
        detachPlayerObservers = ::detachPlayerObservers,
        onPlayerReplaced = { replacementPlayer ->
            player = replacementPlayer
            replacementPlayer.setPauseAtEndOfMediaItems(sleepTimerState.value.option == SleepTimerOption.EndOfSong)
            commandGatewayPlayer = PlaybackExternalCommandGateway(replacementPlayer, ::dispatchPlaybackCommand)
            sessionOwner.setPlayer(commandGatewayPlayer)
            _playerInstanceVersion.value += 1L
        },
        applyPreferredAudioDevice = ::applyPreferredAudioDeviceIfNeeded,
        targetPlayerOutputGain = ::targetPlayerOutputGain,
    )
    private val queueController = PlaybackQueueController(
        runtime = object : PlaybackQueueRuntime {
            override val player: Player
                get() = this@PlaybackManager.player
            override val state: PlaybackUiState
                get() = _state.value

            override fun publishState(state: PlaybackUiState) {
                _state.value = state
            }

            override fun updateState() = this@PlaybackManager.updateState()
            override fun requestAudioFocus(): Boolean = this@PlaybackManager.requestAudioFocus()
            override fun effectivePlayerGain(): Float = this@PlaybackManager.effectivePlayerGain()
            override fun cancelPauseFade(resetVolume: Boolean) = this@PlaybackManager.cancelPauseFade(resetVolume)
            override fun clearInterruptionResumeState() = this@PlaybackManager.clearInterruptionResumeState()
            override fun recordManualPlaybackStart() = this@PlaybackManager.recordManualPlaybackStart()
            override fun stopAndClearQueue() = this@PlaybackManager.stopAndClearQueue()

            override fun resetAudioPathState() {
                _playbackFormatFailure.value = null
                isManualPausePending = false
                isPauseTransitioningToStopped = false
                bitPerfectUsbManager.clearPlaybackFormat()
                lastAppliedAudioPathDecisionKey = null
            }

            override fun resetUnexpectedIdleRecoveryGuard() = this@PlaybackManager.resetUnexpectedIdleRecoveryGuard()
            override fun onQueueReplaced(songs: List<Song>) = queueMetadataRefresher.onQueueReplaced(songs)
            override fun resolveCurrentQueueIndex(state: PlaybackUiState): Int =
                stateReducer.resolveCurrentQueueIndex(state)

            override fun scheduleAudioPathReevaluation(reason: String, delayMs: Long) =
                this@PlaybackManager.scheduleAudioPathReevaluation(reason, delayMs)

            override fun requestFormatFailureReset() {
                _playbackFormatFailure.value = null
            }

            override fun clearFailedPlaybackSongIds() {
                failedPlaybackSongIds.clear()
            }
        },
        queueMetadataRefresher = queueMetadataRefresher,
    )

    init {
        bitPerfectUsbManager.updateEffectsActive(hasActiveSignalAlteringEffects())
        refreshUsbAudioOutputState()
        applyPreferredAudioDeviceIfNeeded(force = true)
        attachPlayerObservers(player)
        syncFromObservedSystemVolume()
        player.volume = effectivePlayerGain()
        syncRuntimeObservers()
        syncProgressUpdateLoop()
    }

    fun reevaluateAudioOutputPath() {
        if (!hasActiveQueue()) return
        bitPerfectUsbManager.updateEffectsActive(hasActiveSignalAlteringEffects())
        refreshUsbAudioOutputState()
        scheduleAudioPathReevaluation("effects-updated", AUDIO_PATH_REEVALUATION_DELAY_MS)
        player.volume = targetPlayerOutputGain()
        updateState()
    }

    fun hasActiveQueue(): Boolean {
        return _state.value.queue.isNotEmpty() || player.mediaItemCount > 0
    }

    internal fun setProgressConsumerActive(
        consumer: PlaybackProgressConsumer,
        active: Boolean,
    ) {
        if (progressDemandController.setActive(consumer, active)) {
            if (active) {
                publishProgressSnapshot(force = true)
            }
            syncProgressUpdateLoop()
        }
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
        return playerFactory.create(enableSignalProcessing)
    }

    private fun currentOffloadPolicy(): PlaybackOffloadPolicy {
        return PlaybackOffloadPolicy.from(
            signalProcessingEnabled = !isDirectPlaybackActive,
            signalAlteringEffectsActive = hasActiveSignalAlteringEffects(),
            gaplessPlaybackEnabled = gaplessPlaybackEnabled,
        )
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

    override fun togglePlayback() {
        dispatchPlaybackCommand(PlaybackCommand.Toggle, PlaybackCommandOrigin.InApp)
    }

    fun dispatchPlaybackCommand(
        command: PlaybackCommand,
        origin: PlaybackCommandOrigin = PlaybackCommandOrigin.InApp,
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            playbackHandler.post { dispatchPlaybackCommand(command, origin) }
            return
        }
        val resolvedCommand = if (command == PlaybackCommand.Toggle) {
            if (isManualPausePending) {
                PlaybackCommand.Play
            } else if (player.isPlaying || player.playWhenReady) {
                PlaybackCommand.Pause
            } else {
                PlaybackCommand.Play
            }
        } else {
            command
        }
        when (resolvedCommand) {
            PlaybackCommand.Play -> {
                if (origin != PlaybackCommandOrigin.AudioInterruption) {
                    clearInterruptionResumeState()
                }
                cancelPauseFade()
                isManualPausePending = false
                isPauseTransitioningToStopped = false
                if (origin == PlaybackCommandOrigin.InApp || origin == PlaybackCommandOrigin.ExternalController) {
                    recordManualPlaybackStart()
                }
                if (origin == PlaybackCommandOrigin.AudioInterruption) {
                    startPlaybackAfterInterruptionWithFadeIn()
                } else {
                    resumePlayback()
                }
            }

            PlaybackCommand.Pause -> {
                if (origin != PlaybackCommandOrigin.AudioInterruption) {
                    clearInterruptionResumeState()
                }
                if (origin != PlaybackCommandOrigin.InApp) {
                    isManualPausePending = false
                    isPauseTransitioningToStopped = true
                    beginPauseFadeOut(
                        if (origin == PlaybackCommandOrigin.BecomingNoisy) {
                            PauseFadeReason.BecomingNoisy
                        } else {
                            PauseFadeReason.AudioInterruption
                        },
                    )
                } else if (!isManualPausePending && (player.isPlaying || player.playWhenReady)) {
                    isPauseTransitioningToStopped = true
                    isManualPausePending = true
                    beginPauseFadeOut(PauseFadeReason.Manual)
                } else if (!player.isPlaying && !player.playWhenReady) {
                    cancelPauseFade(resetVolume = false)
                    isManualPausePending = false
                    isPauseTransitioningToStopped = false
                    player.pause()
                }
            }

            PlaybackCommand.Toggle -> Unit
        }
        updateState()
    }

    override fun seekTo(positionMs: Long) {
        _progressState.value = playbackProgressController.cancelScrub()
        progressDemandController.setActive(PlaybackProgressConsumer.Scrubbing, false)
        player.seekTo(positionMs.coerceAtLeast(0L))
        publishProgressSnapshot(force = true)
        updateState()
    }

    fun beginScrub() {
        progressDemandController.setActive(PlaybackProgressConsumer.Scrubbing, true)
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
        progressDemandController.setActive(PlaybackProgressConsumer.Scrubbing, false)
        syncProgressUpdateLoop()
    }

    fun cancelScrub() {
        _progressState.value = playbackProgressController.cancelScrub()
        progressDemandController.setActive(PlaybackProgressConsumer.Scrubbing, false)
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

    override fun skipNext() {
        cancelPauseFade()
        clearInterruptionResumeState()
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            stopAndClearQueue()
        }
        updateState()
    }

    override fun skipPrevious() {
        cancelPauseFade()
        clearInterruptionResumeState()
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
        queueController.playQueueIndex(index)
    }

    fun enqueueSong(song: Song) {
        queueController.enqueueSong(song)
    }

    fun removeQueueIndex(index: Int) {
        queueController.removeQueueIndex(index)
    }

    fun removeSongsFromQueue(songIds: Set<Long>) {
        queueController.removeSongsFromQueue(songIds)
    }

    fun refreshQueuedLibraryMetadataIfNeeded(updatedSongs: List<Song>) {
        queueController.refreshQueuedLibraryMetadataIfNeeded(updatedSongs)
        bitPerfectUsbManager.updateEffectsActive(hasActiveSignalAlteringEffects())
        scheduleAudioPathReevaluation("queue-metadata-refreshed", AUDIO_PATH_REEVALUATION_DELAY_MS)
        player.volume = targetPlayerOutputGain()
    }

    fun setGaplessPlaybackEnabled(enabled: Boolean) {
        if (gaplessPlaybackEnabled == enabled) return
        gaplessPlaybackEnabled = enabled
        scheduleAudioPathReevaluation("gapless-setting-updated", AUDIO_PATH_REEVALUATION_DELAY_MS)
    }

    fun setVolumeNormalizationEnabled(enabled: Boolean) {
        if (volumeNormalizationEnabled == enabled) return
        volumeNormalizationEnabled = enabled
        bitPerfectUsbManager.updateEffectsActive(hasActiveSignalAlteringEffects())
        scheduleAudioPathReevaluation("volume-normalization-updated", AUDIO_PATH_REEVALUATION_DELAY_MS)
        player.volume = targetPlayerOutputGain()
    }

    fun setSleepTimer(option: SleepTimerOption) {
        sleepTimerController.setTimer(
            option = option,
            currentSongId = currentSong()?.id,
        )
    }

    fun clearSleepTimer() {
        sleepTimerController.clear()
    }

    fun release() {
        if (!released.compareAndSet(false, true)) return
        runtimeTransition = PlaybackRuntimeTransition.Released
        pauseFadeJob?.cancel()
        sleepTimerController.release()
        progressDemandController.clear()
        playbackProgressTicker.release()
        externalInterruptionResumeJob?.cancel()
        playbackHandler.removeCallbacks(audioPathReevaluationRunnable)
        usbDacHardwareVolumeManager.release()
        abandonAudioFocus()
        setNoisyReceiverRegistered(false)
        setAudioDeviceCallbackRegistered(false)
        setVolumeObserverRegistered(false)
        detachPlayerObservers(player)
        sessionOwner.release()
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
        runCatching {
            val currentUsbOutput = currentUsbOutputDescriptor()
            hasUsbOutputRoute = currentUsbOutput != null
            usbDacHardwareVolumeManager.updateAudioOutputDevice(currentUsbOutput)
            bitPerfectUsbManager.refreshConnectedDevices()
        }.onFailure {
            hasUsbOutputRoute = false
            bitPerfectUsbManager.clearPlaybackFormat()
        }
        syncRuntimeObservers()
    }

    private fun applyPreferredAudioDeviceIfNeeded(force: Boolean = false) {
        val preferredDevice = runCatching { bitPerfectUsbManager.preferredOutputDevice() }.getOrNull()
        val nextKey = preferredDevice?.let { device ->
            val id = runCatching { device.id }.getOrNull() ?: return@let null
            val type = runCatching { device.type }.getOrNull() ?: return@let null
            PreferredAudioDeviceKey(id, type)
        }
        if (!force && lastAppliedPreferredDeviceKey == nextKey) return
        if (runCatching { player.setPreferredAudioDevice(preferredDevice) }.isSuccess) {
            lastAppliedPreferredDeviceKey = nextKey
        } else {
            lastAppliedPreferredDeviceKey = null
        }
    }

    private fun maybeRebuildPlayerForAudioPath(reason: String) {
        if (runtimeTransition !is PlaybackRuntimeTransition.Idle) return
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
        runtimeTransition = PlaybackRuntimeTransition.Rebuilding(reason)
        try {
            val previousPlayer = player
            val playbackSnapshot = PlaybackSnapshot.from(previousPlayer)
            val queueSnapshot = _state.value.queue
            logDebug("rebuild direct=$useDirectPlayback reason=$reason position=${playbackSnapshot.positionMs} index=${playbackSnapshot.currentIndex}")
            val replacementPlayer = playerSwitcher.switchPlayerAudioPath(
                currentPlayer = previousPlayer,
                queueSnapshot = queueSnapshot,
                useDirectPlayback = useDirectPlayback,
                playbackSnapshot = playbackSnapshot,
            )
            if (replacementPlayer === previousPlayer) {
                lastAppliedAudioPathDecisionKey = decisionKey.copy(useDirectPlayback = isDirectPlaybackActive)
                player.volume = targetPlayerOutputGain()
                return
            }
            isDirectPlaybackActive = useDirectPlayback
            lastAppliedPreferredDeviceKey = null
            lastAppliedAudioPathDecisionKey = decisionKey
        } finally {
            if (runtimeTransition !is PlaybackRuntimeTransition.Released) {
                runtimeTransition = PlaybackRuntimeTransition.Idle
            }
        }
    }

    private fun setQueue(
        songs: List<Song>,
        startIndex: Int,
        sourceLabel: String?,
        shuffleEnabled: Boolean,
        sourcePlaylistId: Long?,
    ) {
        queueController.setQueue(
            songs = songs,
            startIndex = startIndex,
            sourceLabel = sourceLabel,
            shuffleEnabled = shuffleEnabled,
            sourcePlaylistId = sourcePlaylistId,
            audioPathDelayMs = AUDIO_PATH_REEVALUATION_DELAY_MS,
        )
        sleepTimerController.updateEndOfSongTarget(songs.getOrNull(startIndex)?.id)
    }

    private fun stopAndClearQueue() {
        cancelPauseFade(resetVolume = false)
        sleepTimerController.clear()
        isStoppingQueue = true
        clearInterruptionResumeState()
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
        stateReducer.clearCurrentSongTracking()
        failedPlaybackSongIds.clear()
        _state.value = _state.value.copy(
            queue = emptyList(),
            currentIndex = -1,
            isPlaying = false,
            transportShowsPause = false,
            sourceLabel = null,
            audioSessionId = 0,
            sourcePlaylistId = null,
        )
        queueMetadataRefresher.reset()
        _progressState.value = playbackProgressController.clear()
        syncRuntimeObservers()
        syncProgressUpdateLoop()
        resetUnexpectedIdleRecoveryGuard()
        isStoppingQueue = false
    }

    private fun updateState() {
        val existingState = _state.value
        val currentIndex = stateReducer.resolveCurrentQueueIndex(existingState)
        val currentSong = existingState.queue.getOrNull(currentIndex)
        if (currentSong != null && (player.isPlaying || player.playWhenReady)) {
            resetUnexpectedIdleRecoveryGuard()
        }
        userVolume = currentEffectiveVolumeFraction()
        val updatedState = stateReducer.reduce(
            existingState = existingState,
            isPauseTransitioningToStopped = isPauseTransitioningToStopped,
        )
        if (publishPlaybackState(updatedState, existingState)) {
            stateReducer.notifyRecentPlaybackChanged(updatedState, existingState)
        }
        syncRuntimeObservers()
        publishProgressSnapshot()
    }

    private fun publishPlaybackState(
        nextState: PlaybackUiState,
        currentState: PlaybackUiState = _state.value,
    ): Boolean {
        if (nextState == currentState) return false
        _state.value = nextState
        return true
    }

    private fun handleUnsupportedPlaybackFormat(error: PlaybackException): Boolean {
        val song = currentSong() ?: return false
        if (!failedPlaybackSongIds.add(song.id)) {
            logDebug("Repeated unsupported playback failure ignored")
            return true
        }
        val classification = PlaybackFailureClassifier.classify(error)
        _playbackFormatFailure.value = PlaybackFormatFailure(
            songId = song.id,
            fileName = song.fileName,
            format = song.audioFormat,
            reason = classification.userSafeReason,
        )
        logDebug("Unsupported playback format category=${classification.category} code=${error.errorCodeName}")
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.prepare()
            if (requestAudioFocus()) {
                player.play()
            }
            updateState()
        } else {
            stopAndClearQueue()
        }
        return true
    }

    private fun publishProgressSnapshot(force: Boolean = false) {
        val updatedProgress = ElovaireTrace.section("playback_progress_publish") {
            playbackProgressController.onPlayerSnapshot(
                mediaId = currentSong()?.id,
                positionMs = player.currentPosition.coerceAtLeast(0L),
                durationMs = player.duration.takeIf { it > 0 }?.coerceAtLeast(0L) ?: 0L,
                bufferedPositionMs = player.bufferedPosition.coerceAtLeast(0L),
                isPlaying = if (isPauseTransitioningToStopped) false else player.isPlaying,
            )
        }
        if (force || updatedProgress != _progressState.value) {
            _progressState.value = updatedProgress
        }
        syncProgressUpdateLoop()
    }

    private fun resumePlayback() {
        externalInterruptionResumeJob?.cancel()
        if (!interruptionResumeState.isActive) {
            pendingAutoResumeRetryJob?.cancel()
            pendingAutoResumeRetryJob = null
        }
        startPlaybackWithFadeIn(ResumeFadeReason.Manual)
    }

    private fun startPlaybackAfterInterruptionWithFadeIn() {
        startPlaybackWithFadeIn(ResumeFadeReason.AudioInterruption)
    }

    private fun startPlaybackWithFadeIn(reason: ResumeFadeReason) {
        if (!requestAudioFocus()) {
            if (reason == ResumeFadeReason.AudioInterruption) {
                scheduleAutoResumeRetry("fade-in-focus-failed")
            }
            return
        }

        cancelPauseFade(resetVolume = false)
        bitPerfectUsbManager.updateEffectsActive(hasActiveSignalAlteringEffects())
        refreshUsbAudioOutputState()
        scheduleAudioPathReevaluation(
            when (reason) {
                ResumeFadeReason.Manual -> "resume-playback"
                ResumeFadeReason.AudioInterruption -> "resume-after-interruption"
                ResumeFadeReason.Recovery -> "resume-recovery"
            },
        )
        isPauseTransitioningToStopped = false
        isManualPausePending = false

        val targetGain = effectivePlayerGain()
        if (!supportsSoftwarePlaybackFade()) {
            player.volume = targetGain
            player.play()
            if (reason == ResumeFadeReason.AudioInterruption) {
                clearInterruptionResumeState()
            } else {
                shouldResumeAfterTransientFocusLoss = false
                pausedForAudioFocusLoss = false
                pendingResumeAfterExternalInterruption = false
            }
            updateState()
            return
        }

        player.volume = 0f
        player.play()
        updateState()

        pauseFadeJob?.cancel()
        pauseFadeJob = scope.launch {
            repeat(PAUSE_FADE_STEP_COUNT) { step ->
                if (!isActive) return@launch
                val progress = (step + 1).toFloat() / PAUSE_FADE_STEP_COUNT.toFloat()
                player.volume = lerp(
                    start = 0f,
                    stop = targetGain,
                    fraction = progress,
                )
                delay(PAUSE_FADE_STEP_DURATION_MS)
            }
            player.volume = targetGain
            pauseFadeJob = null
            if (reason == ResumeFadeReason.AudioInterruption) {
                clearInterruptionResumeState()
            } else {
                shouldResumeAfterTransientFocusLoss = false
                pausedForAudioFocusLoss = false
                pendingResumeAfterExternalInterruption = false
            }
            updateState()
        }
    }

    private fun beginPauseFadeOut(reason: PauseFadeReason) {
        if (!supportsSoftwarePlaybackFade()) {
            player.pause()
            if (reason == PauseFadeReason.Manual || reason == PauseFadeReason.BecomingNoisy) {
                abandonAudioFocus()
            }
            if (reason == PauseFadeReason.Manual) {
                isManualPausePending = false
            }
            isPauseTransitioningToStopped = false
            player.volume = targetPlayerOutputGain()
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
            if (reason == PauseFadeReason.Manual || reason == PauseFadeReason.BecomingNoisy) {
                abandonAudioFocus()
            }
            if (reason == PauseFadeReason.Manual) {
                isManualPausePending = false
            }
            isPauseTransitioningToStopped = true
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

    private fun supportsSoftwarePlaybackFade(): Boolean {
        return !isDirectPlaybackActive && !usbDacHardwareVolumeManager.shouldBypassSoftwareVolume()
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
        when (AudioFocusStateMachine.actionFor(focusChange)) {
            AudioFocusAction.Gain -> {
                hasAudioFocus = true
                pendingAutoResumeRetryJob?.cancel()
                pendingAutoResumeRetryJob = null
                if (shouldKeepInterruptionResumeIntent()) {
                    scope.launch {
                        delay(AUTO_RESUME_SETTLE_DELAY_MS)
                        attemptAutoResumeAfterInterruption("focus-gain")
                    }
                } else {
                    clearInterruptionResumeState()
                    player.volume = effectivePlayerGain()
                    updateState()
                }
            }

            AudioFocusAction.TransientLoss -> {
                handleTransientFocusLoss()
            }

            AudioFocusAction.PermanentLoss -> {
                val wasAudiblyPlaying = player.isPlaying ||
                    player.playWhenReady ||
                    _state.value.transportShowsPause
                val looksLikeExternalMediaInterruption = wasAudiblyPlaying &&
                    _state.value.queue.isNotEmpty() &&
                    hasActiveExternalMediaPlayback()
                if (looksLikeExternalMediaInterruption) {
                    markInterruptedForResume(InterruptionResumeReason.PermanentLossWithActiveExternalMedia)
                    scheduleExternalInterruptionResumeWatch()
                } else {
                    clearInterruptionResumeState()
                }
                isManualPausePending = false
                isPauseTransitioningToStopped = true
                beginPauseFadeOut(PauseFadeReason.AudioInterruption)
                abandonAudioFocus()
            }

            AudioFocusAction.Ignore -> Unit
        }
    }

    private fun handleTransientFocusLoss() {
        val wasAudiblyPlaying = player.isPlaying ||
            player.playWhenReady ||
            _state.value.transportShowsPause
        if (wasAudiblyPlaying && _state.value.queue.isNotEmpty()) {
            markInterruptedForResume(InterruptionResumeReason.TransientFocusLoss)
        }
        isManualPausePending = false
        isPauseTransitioningToStopped = true
        beginPauseFadeOut(PauseFadeReason.AudioInterruption)
        scheduleExternalInterruptionResumeWatch()
    }

    private fun recoverUnexpectedIdleState(shouldAutoPlay: Boolean) {
        val snapshot = _state.value
        if (snapshot.queue.isEmpty()) return
        if (!registerUnexpectedIdleRecoveryAttempt()) {
            enterSafeStoppedStateAfterRecoveryFailure(snapshot)
            return
        }
        val recoverIndex = stateReducer.lastKnownQueueIndex
            .takeIf { it >= 0 }
            .takeIf { it in snapshot.queue.indices }
            ?: snapshot.currentIndex.takeIf { it in snapshot.queue.indices }
            ?: 0
        val recoverPosition = stateReducer.lastKnownPositionMs.coerceAtLeast(0L)
        bitPerfectUsbManager.clearPlaybackFormat()
        lastAppliedAudioPathDecisionKey = null
        scheduleAudioPathReevaluation("recover-idle", AUDIO_PATH_REEVALUATION_DELAY_MS)
        runtimeTransition = PlaybackRuntimeTransition.Recovering(unexpectedIdleRecoveryCount)
        scope.launch {
            val mediaItems = snapshot.queue.mapTo(ArrayList(snapshot.queue.size)) { song ->
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
            if (runtimeTransition !is PlaybackRuntimeTransition.Released) {
                runtimeTransition = PlaybackRuntimeTransition.Idle
                scheduleAudioPathReevaluation("recovery-completed")
            }
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
        if (runtimeTransition !is PlaybackRuntimeTransition.Released) {
            runtimeTransition = PlaybackRuntimeTransition.Idle
        }
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

    private fun isPausedAtEndOfCurrentSong(): Boolean {
        if (!player.getPauseAtEndOfMediaItems() || player.playWhenReady || player.isPlaying) return false
        val duration = player.duration.takeIf { it > 0L } ?: return false
        val position = player.currentPosition.coerceAtLeast(0L)
        return duration - position <= END_OF_SONG_TIMER_TOLERANCE_MS
    }

    private fun syncProgressUpdateLoop() {
        if (!shouldPollProgress()) {
            playbackProgressTicker.stop()
            return
        }
        playbackProgressTicker.start()
    }

    private fun shouldPollProgress(): Boolean {
        return hasActiveQueue() && (
            playbackProgressController.needsActivePolling() ||
                (player.isPlaying && progressDemandController.hasAnyDemand())
            )
    }

    private fun scheduleStatePublish() {
        if (statePublishScheduled) return
        statePublishScheduled = true
        playbackHandler.post {
            statePublishScheduled = false
            updateState()
        }
    }

    private fun shouldAutoResumeAfterUnexpectedIdle(): Boolean {
        val snapshot = _state.value
        return snapshot.queue.isNotEmpty() &&
            !isManualPausePending &&
            (snapshot.transportShowsPause || snapshot.isPlaying || player.playWhenReady || shouldResumeAfterTransientFocusLoss)
    }

    private fun clearInterruptionResumeState() {
        shouldResumeAfterTransientFocusLoss = false
        pausedForAudioFocusLoss = false
        pendingResumeAfterExternalInterruption = false
        interruptionResumeState = InterruptionResumeState()
        externalInterruptionResumeJob?.cancel()
        externalInterruptionResumeJob = null
        pendingAutoResumeRetryJob?.cancel()
        pendingAutoResumeRetryJob = null
    }

    private fun markInterruptedForResume(reason: InterruptionResumeReason) {
        if (_state.value.queue.isEmpty()) return
        interruptionResumeState = InterruptionResumeState(
            shouldResume = true,
            reason = reason,
            startedAtElapsedMs = SystemClock.elapsedRealtime(),
            resumeAttempts = 0,
        )
        shouldResumeAfterTransientFocusLoss = true
        pausedForAudioFocusLoss = true
        pendingResumeAfterExternalInterruption =
            reason != InterruptionResumeReason.TransientFocusLoss || hasActiveExternalMediaPlayback()
    }

    private fun shouldKeepInterruptionResumeIntent(): Boolean {
        val state = interruptionResumeState
        if (!state.shouldResume) return false
        if (_state.value.queue.isEmpty()) return false
        if (isManualPausePending || isStoppingQueue) return false
        val elapsedMs = SystemClock.elapsedRealtime() - state.startedAtElapsedMs
        return elapsedMs <= EXTERNAL_INTERRUPTION_MAX_WATCH_MS
    }

    private fun attemptAutoResumeAfterInterruption(trigger: String) {
        if (!shouldKeepInterruptionResumeIntent()) {
            clearInterruptionResumeState()
            return
        }
        if (player.isPlaying || player.playWhenReady) {
            clearInterruptionResumeState()
            updateState()
            return
        }
        if (hasActiveExternalMediaPlayback()) {
            scheduleExternalInterruptionResumeWatch()
            return
        }
        if (!requestAudioFocus()) {
            scheduleAutoResumeRetry(trigger)
            return
        }
        startPlaybackAfterInterruptionWithFadeIn()
    }

    private fun scheduleAutoResumeRetry(trigger: String) {
        if (!shouldKeepInterruptionResumeIntent()) {
            clearInterruptionResumeState()
            return
        }
        val attempts = interruptionResumeState.resumeAttempts + 1
        interruptionResumeState = interruptionResumeState.copy(resumeAttempts = attempts)
        if (attempts > MAX_AUTO_RESUME_FOCUS_ATTEMPTS) {
            clearInterruptionResumeState()
            updateState()
            return
        }
        pendingAutoResumeRetryJob?.cancel()
        pendingAutoResumeRetryJob = scope.launch {
            delay(AUTO_RESUME_FOCUS_RETRY_DELAY_MS)
            if (shouldKeepInterruptionResumeIntent()) {
                attemptAutoResumeAfterInterruption("$trigger-retry")
            }
        }
    }

    private fun scheduleExternalInterruptionResumeWatch() {
        if (!shouldKeepInterruptionResumeIntent() || externalInterruptionResumeJob?.isActive == true) return
        externalInterruptionResumeJob = scope.launch {
            val startedAtMs = SystemClock.elapsedRealtime()
            var quietConfirmations = 0
            while (isActive && shouldKeepInterruptionResumeIntent()) {
                val elapsedMs = SystemClock.elapsedRealtime() - startedAtMs
                if (elapsedMs >= EXTERNAL_INTERRUPTION_MAX_WATCH_MS) {
                    clearInterruptionResumeState()
                    break
                }
                if (
                    _state.value.queue.isEmpty() ||
                    isManualPausePending ||
                    player.isPlaying
                ) {
                    clearInterruptionResumeState()
                    break
                }
                if (hasActiveExternalMediaPlayback()) {
                    quietConfirmations = 0
                } else {
                    quietConfirmations += 1
                    if (quietConfirmations >= EXTERNAL_INTERRUPTION_QUIET_CONFIRMATIONS) {
                        attemptAutoResumeAfterInterruption("external-watch")
                        break
                    }
                }
                delay(
                    if (elapsedMs < EXTERNAL_INTERRUPTION_FAST_WATCH_MS) {
                        EXTERNAL_INTERRUPTION_FAST_DELAY_MS
                    } else {
                        EXTERNAL_INTERRUPTION_BACKOFF_DELAY_MS
                    },
                )
            }
        }
    }

    private fun syncRuntimeObservers() {
        val hasQueue = hasActiveQueue()
        val wantsPlaybackRuntime = hasQueue || player.isPlaying || player.playWhenReady
        val wantsNoisyReceiver = player.isPlaying || player.playWhenReady
        setVolumeObserverRegistered(wantsPlaybackRuntime)
        setAudioDeviceCallbackRegistered(wantsPlaybackRuntime || hasUsbOutputRoute)
        setNoisyReceiverRegistered(wantsNoisyReceiver)
    }

    private fun setVolumeObserverRegistered(registered: Boolean) {
        if (volumeObserverRegistered == registered) return
        if (registered) {
            if (
                runCatching {
                    appContext.contentResolver.registerContentObserver(
                        Settings.System.CONTENT_URI,
                        true,
                        systemVolumeObserver,
                    )
                }.isFailure
            ) {
                return
            }
        } else {
            runCatching { appContext.contentResolver.unregisterContentObserver(systemVolumeObserver) }
        }
        volumeObserverRegistered = registered
    }

    private fun setAudioDeviceCallbackRegistered(registered: Boolean) {
        if (audioDeviceCallbackRegistered == registered) return
        if (registered) {
            if (runCatching { audioManager?.registerAudioDeviceCallback(audioDeviceCallback, playbackHandler) }.isFailure) {
                return
            }
        } else {
            runCatching { audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback) }
        }
        audioDeviceCallbackRegistered = registered
    }

    private fun setNoisyReceiverRegistered(registered: Boolean) {
        if (noisyReceiverRegistered == registered) return
        if (registered) {
            if (
                runCatching {
                    ContextCompat.registerReceiver(
                        appContext,
                        becomingNoisyReceiver,
                        IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                        ContextCompat.RECEIVER_NOT_EXPORTED,
                    )
                }.isFailure
            ) {
                return
            }
        } else {
            runCatching { appContext.unregisterReceiver(becomingNoisyReceiver) }
        }
        noisyReceiverRegistered = registered
    }

    private fun hasActiveExternalMediaPlayback(): Boolean {
        return runCatching { audioManager?.isMusicActive == true }.getOrDefault(false)
    }

    private fun recordManualPlaybackStart() {
        _manualPlaybackStartVersion.value = _manualPlaybackStartVersion.value + 1L
    }

    private fun effectivePlayerGain(): Float {
        val baseGain = if (usesFixedVolumeOutput()) userVolume else volumeFineGain
        return VolumeNormalizationPolicy.effectivePlayerGain(
            baseGain = baseGain,
            metadata = currentSong()?.volumeNormalization,
            enabled = volumeNormalizationEnabled,
            softwareGainAllowed = !isDirectPlaybackActive && !usbDacHardwareVolumeManager.shouldBypassSoftwareVolume(),
        )
    }

    private fun hasActiveSignalAlteringEffects(): Boolean {
        return hasSignalAlteringEffects() ||
            VolumeNormalizationPolicy.isSignalAltering(
                metadata = currentSong()?.volumeNormalization,
                enabled = volumeNormalizationEnabled,
            )
    }

    private fun audioPathReevaluationDelayForTransition(): Long {
        return if (gaplessPlaybackEnabled) {
            GAPLESS_TRANSITION_AUDIO_PATH_REEVALUATION_DELAY_MS
        } else {
            AUDIO_PATH_REEVALUATION_DELAY_MS
        }
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
            val maxStep = runCatching {
                manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            }.getOrDefault(1).coerceAtLeast(1)
            val targetSystemStep = (targetVolume * maxStep.toFloat()).roundToInt().coerceIn(0, maxStep)
            ignoreObservedSystemVolumeStep = targetSystemStep
            runCatching { manager.setStreamVolume(AudioManager.STREAM_MUSIC, targetSystemStep, 0) }
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
        val maxStep = runCatching {
            manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }.getOrDefault(1).coerceAtLeast(1)
        if (targetVolume <= 0f) {
            ignoreObservedSystemVolumeStep = 0
            runCatching { manager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0) }
            volumeFineGain = 0f
            userVolume = 0f
            return
        }

        val exactSteps = targetVolume * maxStep.toFloat()
        val targetSystemStep = ceil(exactSteps).toInt().coerceIn(1, maxStep)
        ignoreObservedSystemVolumeStep = targetSystemStep
        runCatching { manager.setStreamVolume(AudioManager.STREAM_MUSIC, targetSystemStep, 0) }
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
        if (usbDacHardwareVolumeManager.shouldOwnVolumeControls()) {
            return usbDacHardwareVolumeManager.currentHardwareVolume() ?: userVolume
        }
        if (isDirectPlaybackActive) {
            return currentSystemVolumeFraction().quantizedVolume()
        }
        if (usesFixedVolumeOutput()) {
            return userVolume
        }
        return currentEffectiveVolumeFraction()
    }

    private fun shouldBypassSystemStreamVolume(): Boolean {
        return usbDacHardwareVolumeManager.shouldOwnVolumeControls() ||
            usesFixedVolumeOutput()
    }

    private fun usesFixedVolumeOutput(): Boolean {
        return runCatching { audioManager?.isVolumeFixed == true }.getOrDefault(false)
    }

    private fun currentSystemVolumeStep(): Int {
        val manager = audioManager ?: return 0
        return runCatching {
            manager.getStreamVolume(AudioManager.STREAM_MUSIC)
        }.getOrDefault(0).coerceAtLeast(0)
    }

    private fun currentSystemVolumeFraction(): Float {
        val manager = audioManager ?: return 1f
        val maxStep = runCatching {
            manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }.getOrDefault(1).coerceAtLeast(1)
        val currentStep = currentSystemVolumeStep()
        return currentStep.toFloat() / maxStep.toFloat()
    }

    private fun currentUsbOutputDescriptor(): UsbAudioDeviceDescriptor? {
        val manager = audioManager ?: return null
        val routedUsbDevice = if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            supportsVerifiedDirectPlaybackRouting(Build.VERSION.SDK_INT)
        ) {
            manager.safeRoutedOutputDevicesForAttributes(platformPlaybackAudioAttributes)
                .firstOrNull { device ->
                    runCatching { device.type in USB_AUDIO_OUTPUT_DEVICE_TYPES }.getOrDefault(false)
                }
        } else {
            null
        }
        return (routedUsbDevice ?: manager.safeOutputDevices()
            .firstOrNull { device ->
                runCatching { device.type in USB_AUDIO_OUTPUT_DEVICE_TYPES }.getOrDefault(false)
            })
            ?.toUsbAudioDeviceDescriptor()
    }

    private fun resolveCurrentQueueIndex(existingState: PlaybackUiState): Int {
        return stateReducer.resolveCurrentQueueIndex(existingState)
    }

    private companion object {
        const val PAUSE_FADE_DURATION_MS = 600L
        const val PAUSE_FADE_STEP_COUNT = 20
        const val PAUSE_FADE_STEP_DURATION_MS = PAUSE_FADE_DURATION_MS / PAUSE_FADE_STEP_COUNT
        const val PREVIOUS_SEEK_THRESHOLD_MS = 5_000L
        const val END_OF_SONG_TIMER_TOLERANCE_MS = 350L
        const val AUDIO_PATH_REEVALUATION_DELAY_MS = 80L
        const val GAPLESS_TRANSITION_AUDIO_PATH_REEVALUATION_DELAY_MS = 650L
        const val EXTERNAL_INTERRUPTION_FAST_WATCH_MS = 10_000L
        const val EXTERNAL_INTERRUPTION_MAX_WATCH_MS = 5 * 60_000L
        const val EXTERNAL_INTERRUPTION_FAST_DELAY_MS = 500L
        const val EXTERNAL_INTERRUPTION_BACKOFF_DELAY_MS = 3_000L
        const val EXTERNAL_INTERRUPTION_QUIET_CONFIRMATIONS = 2
        const val MAX_AUTO_RESUME_FOCUS_ATTEMPTS = 12
        const val AUTO_RESUME_FOCUS_RETRY_DELAY_MS = 350L
        const val AUTO_RESUME_SETTLE_DELAY_MS = 180L
        const val MAX_UNEXPECTED_IDLE_RECOVERY_ATTEMPTS = 3
        const val UNEXPECTED_IDLE_RECOVERY_WINDOW_MS = 10_000L
        const val TAG = "PlaybackManager"
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

internal fun Song.toPlaybackMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMimeType(inferPlaybackMimeType())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setAlbumArtist(albumArtist ?: artist)
                .setTrackNumber(trackNumber.takeIf { it > 0 })
                .setDiscNumber(discNumber.takeIf { it > 0 })
                .setReleaseYear(releaseYear)
                .setGenre(genre.takeIf(String::isNotBlank))
                .setDurationMs(durationMs.takeIf { it > 0 })
                .setArtworkUri(artUri)
                .setIsPlayable(true)
                .setIsBrowsable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .build(),
        )
        .build()
}

internal fun Song.playbackMetadataSignature(): Int {
    var result = id.hashCode()
    result = 31 * result + title.hashCode()
    result = 31 * result + artist.hashCode()
    result = 31 * result + album.hashCode()
    result = 31 * result + (albumArtist?.hashCode() ?: 0)
    result = 31 * result + (releaseYear ?: 0)
    result = 31 * result + genre.hashCode()
    result = 31 * result + durationMs.hashCode()
    result = 31 * result + trackNumber
    result = 31 * result + discNumber
    result = 31 * result + uri.hashCode()
    result = 31 * result + (artUri?.hashCode() ?: 0)
    result = 31 * result + fileName.hashCode()
    result = 31 * result + (volumeNormalization?.hashCode() ?: 0)
    return result
}

private fun Song.inferPlaybackMimeType(): String? {
    return AudioFormatPolicy.playbackMimeType(fileName)
}

private fun PlaybackException.isUnsupportedFormatError(): Boolean {
    return errorCode == PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ||
        errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
        errorCode == PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED
}

private fun Float.quantizedVolume(): Float {
    return ((coerceIn(0f, 1f) * 100f).roundToInt() / 100f).coerceIn(0f, 1f)
}

private fun Array<out AudioDeviceInfo>.hasUsbOutputDeviceChange(): Boolean {
    return any { device ->
        runCatching { device.isSink && device.type in USB_AUDIO_OUTPUT_DEVICE_TYPES }.getOrDefault(false)
    }
}

internal data class PlaybackSnapshot(
    val currentIndex: Int,
    val positionMs: Long,
    val playWhenReady: Boolean,
    val playbackParameters: PlaybackParameters,
) {
    companion object {
        fun from(player: Player): PlaybackSnapshot {
            return PlaybackSnapshot(
                currentIndex = player.currentMediaItemIndex.coerceAtLeast(0),
                positionMs = player.currentPosition.coerceAtLeast(0L),
                playWhenReady = player.playWhenReady,
                playbackParameters = player.playbackParameters,
            )
        }
    }
}

internal sealed interface PlaybackRuntimeTransition {
    data object Idle : PlaybackRuntimeTransition
    data class Rebuilding(val reason: String) : PlaybackRuntimeTransition
    data class Recovering(val attempt: Int) : PlaybackRuntimeTransition
    data object Released : PlaybackRuntimeTransition
}

private data class PreferredAudioDeviceKey(
    val id: Int,
    val type: Int,
)

private fun AudioDeviceInfo.toUsbAudioDeviceDescriptor(): UsbAudioDeviceDescriptor {
    return UsbAudioDeviceDescriptor(
        id = runCatching { id }.getOrDefault(-1),
        type = runCatching { type }.getOrDefault(-1),
        isSink = runCatching { isSink }.getOrDefault(false),
        productName = runCatching { productName?.toString() }.getOrNull(),
        address = runCatching { address.takeIf { it.isNotBlank() } }.getOrNull(),
        sampleRates = runCatching { sampleRates.copyOf() }.getOrDefault(intArrayOf()),
        encodings = runCatching { encodings.copyOf() }.getOrDefault(intArrayOf()),
        channelCounts = runCatching { channelCounts.copyOf() }.getOrDefault(intArrayOf()),
        channelMasks = runCatching { channelMasks.copyOf() }.getOrDefault(intArrayOf()),
    )
}

internal fun Int.toPlaybackRepeatMode(): PlaybackRepeatMode {
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
