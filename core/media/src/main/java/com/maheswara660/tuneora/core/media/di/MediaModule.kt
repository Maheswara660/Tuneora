package com.maheswara660.tuneora.core.media.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import com.maheswara660.tuneora.core.common.MediaSynchronizer
import com.maheswara660.tuneora.core.media.sync.LocalMediaSynchronizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import androidx.media3.common.Format
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessorChain
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.maheswara660.tuneora.core.media.audio.ReplayGainAudioProcessor
import com.maheswara660.tuneora.core.media.audio.EndedWorkaroundPlayer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @Singleton
    fun provideReplayGainAudioProcessor(): ReplayGainAudioProcessor = ReplayGainAudioProcessor()

    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
        replayGainAudioProcessor: ReplayGainAudioProcessor
    ): ExoPlayer {
        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink? {
                return DefaultAudioSink.Builder(context)
                    .setAudioProcessors(arrayOf(replayGainAudioProcessor))
                    .build()
            }
        }

        return ExoPlayer.Builder(context, renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideEndedWorkaroundPlayer(
        player: ExoPlayer
    ): EndedWorkaroundPlayer = EndedWorkaroundPlayer(player)
    @Provides
    @Singleton
    fun provideMediaSynchronizer(
        localMediaSynchronizer: LocalMediaSynchronizer
    ): MediaSynchronizer = localMediaSynchronizer
}

