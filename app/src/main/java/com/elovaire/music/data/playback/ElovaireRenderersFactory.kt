package elovaire.music.droidbeauty.app.data.playback

import android.content.Context
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

@UnstableApi
internal class ElovaireRenderersFactory(
    context: Context,
    private val audioProcessors: Array<AudioProcessor>,
    private val processingRequired: () -> Boolean = { true },
) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioOutputPlaybackParameters: Boolean,
    ): AudioSink {
        val sink = DefaultAudioSink.Builder(context)
            .setAudioProcessors(audioProcessors)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioOutputPlaybackParameters(
                enableAudioOutputPlaybackParameters && audioProcessors.isNotEmpty(),
            )
            .build()
        return if (enableFloatOutput) {
            PrecisionSwitchingAudioSink(
                sink = sink,
                processingRequired = processingRequired,
                processorsSettled = {
                    audioProcessors.all { it is EqualizerAudioProcessor && it.isSettledForBypass }
                },
            )
        } else sink
    }
}
