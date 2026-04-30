package elovaire.music.app.data.playback

import android.content.Context
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink

internal class ElovaireRenderersFactory(
    context: Context,
    private val audioProcessors: Array<AudioProcessor>,
) : DefaultRenderersFactory(context) {
    override fun buildAudioSink(
        context: Context,
        enableFloatOutput: Boolean,
        enableAudioOutputPlaybackParameters: Boolean,
    ): AudioSink {
        return DefaultAudioSink.Builder(context)
            .setAudioProcessors(audioProcessors)
            .setEnableFloatOutput(enableFloatOutput)
            .setEnableAudioOutputPlaybackParameters(enableAudioOutputPlaybackParameters)
            .build()
    }
}
