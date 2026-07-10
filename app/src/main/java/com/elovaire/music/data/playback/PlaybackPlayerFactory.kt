package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import android.media.AudioDeviceInfo
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.common.util.UnstableApi
import elovaire.music.droidbeauty.app.core.performance.ElovaireTrace

@UnstableApi
internal class PlaybackPlayerFactory(
    private val context: Context,
    private val dataSourceFactory: DefaultDataSource.Factory,
    private val extractorsFactory: DefaultExtractorsFactory,
    private val playbackAudioAttributes: AudioAttributes,
    private val audioProcessorsProvider: () -> Array<AudioProcessor>,
    private val preferredOutputDevice: () -> AudioDeviceInfo?,
    private val offloadPolicyProvider: () -> PlaybackOffloadPolicy = { PlaybackOffloadPolicy.Disabled },
) {
    fun create(enableSignalProcessing: Boolean): ExoPlayer {
        return ElovaireTrace.section("playback_player_create") {
            runCatching {
                createConfiguredPlayer(enableSignalProcessing)
            }.getOrElse {
                createFallbackPlayer()
            }
        }
    }

    private fun createConfiguredPlayer(enableSignalProcessing: Boolean): ExoPlayer {
        val offloadPreferences = resolveOffloadPreferences(enableSignalProcessing)
        val configuredPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(
                ElovaireRenderersFactory(
                    context,
                    if (enableSignalProcessing) audioProcessorsProvider() else emptyArray(),
                )
                    .setEnableAudioFloatOutput(false)
                    .setEnableDecoderFallback(true)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER),
            )
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context, extractorsFactory)
                    .setDataSourceFactory(dataSourceFactory),
            )
            .setAudioAttributes(playbackAudioAttributes, false)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(false)
            .build()
            .apply {
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_OFF
                applyAudioOffloadPreferences(offloadPreferences)
            }
        applyPreferredOutputDevice(configuredPlayer)
        return configuredPlayer
    }

    private fun createFallbackPlayer(): ExoPlayer {
        val fallbackPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(context, extractorsFactory)
                    .setDataSourceFactory(dataSourceFactory),
            )
            .setAudioAttributes(playbackAudioAttributes, false)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(false)
            .build()
            .apply {
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_OFF
            }
        applyPreferredOutputDevice(fallbackPlayer)
        return fallbackPlayer
    }

    private fun resolveOffloadPreferences(
        enableSignalProcessing: Boolean,
    ): TrackSelectionParameters.AudioOffloadPreferences {
        return runCatching {
            if (enableSignalProcessing) {
                offloadPolicyProvider().toAudioOffloadPreferences()
            } else {
                PlaybackOffloadPolicy.Disabled.toAudioOffloadPreferences()
            }
        }.getOrDefault(PlaybackOffloadPolicy.Disabled.toAudioOffloadPreferences())
    }

    private fun ExoPlayer.applyAudioOffloadPreferences(
        offloadPreferences: TrackSelectionParameters.AudioOffloadPreferences,
    ) {
        runCatching {
            setTrackSelectionParameters(
                trackSelectionParameters.withAudioOffloadPreferences(offloadPreferences),
            )
        }
    }

    private fun applyPreferredOutputDevice(player: ExoPlayer) {
        val preferredDevice = runCatching { preferredOutputDevice() }.getOrNull()
        runCatching { preferredDevice?.let(player::setPreferredAudioDevice) }
    }
}

@UnstableApi
private fun TrackSelectionParameters.withAudioOffloadPreferences(
    audioOffloadPreferences: TrackSelectionParameters.AudioOffloadPreferences,
): TrackSelectionParameters {
    if (this.audioOffloadPreferences == audioOffloadPreferences) return this
    return buildUpon()
        .setAudioOffloadPreferences(audioOffloadPreferences)
        .build()
}
