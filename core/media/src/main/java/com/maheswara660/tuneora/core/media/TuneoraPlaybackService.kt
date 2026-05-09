package com.maheswara660.tuneora.core.media

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.maheswara660.tuneora.core.data.repository.MusicRepository
import com.maheswara660.tuneora.core.data.repository.PreferencesRepository
import com.maheswara660.tuneora.core.media.audio.EndedWorkaroundPlayer
import com.maheswara660.tuneora.core.media.audio.LastPlayedManager
import com.maheswara660.tuneora.core.common.model.SemanticLyrics
import com.maheswara660.tuneora.core.media.repository.lyrics.LyricsRepository
import com.maheswara660.tuneora.core.media.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class TuneoraPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var musicRepository: MusicRepository
    
    @Inject
    lateinit var lyricsRepository: LyricsRepository

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private lateinit var player: EndedWorkaroundPlayer
    private lateinit var lastPlayedManager: LastPlayedManager
    private var isCurrentTrackFavorite = false

    companion object {
        const val ACTION_FAVORITE = "com.maheswara660.tuneora.ACTION_FAVORITE"
        const val ACTION_SHUFFLE = "com.maheswara660.tuneora.ACTION_SHUFFLE"
        const val ACTION_REPEAT = "com.maheswara660.tuneora.ACTION_REPEAT"
        const val ACTION_STOP = "com.maheswara660.tuneora.ACTION_STOP"
        const val SERVICE_GET_LYRICS = "get_lyrics"
        
        val COMMAND_FAVORITE = SessionCommand(ACTION_FAVORITE, Bundle.EMPTY)
        val COMMAND_SHUFFLE = SessionCommand(ACTION_SHUFFLE, Bundle.EMPTY)
        val COMMAND_REPEAT = SessionCommand(ACTION_REPEAT, Bundle.EMPTY)
        val COMMAND_STOP = SessionCommand(ACTION_STOP, Bundle.EMPTY)
        val COMMAND_GET_LYRICS = SessionCommand(SERVICE_GET_LYRICS, Bundle.EMPTY)
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        
        player = EndedWorkaroundPlayer(exoPlayer)
        lastPlayedManager = LastPlayedManager(this, player)

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(TuneoraSessionCallback())
            .build()

        notificationProvider = TuneoraNotificationProvider()
        setMediaNotificationProvider(notificationProvider)

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                lastPlayedManager.save()
                mediaItem?.mediaId?.toLongOrNull()?.let { id ->
                    serviceScope.launch {
                        musicRepository.updateLastPlayed(id)
                        isCurrentTrackFavorite = musicRepository.getSongById(id)?.isFavorite == true
                        mediaSession?.let { updateNotification(it) }
                    }
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                lastPlayedManager.save()
                if (playbackState == Player.STATE_READY && player.isPlaying) {
                    player.currentMediaItem?.mediaId?.toLongOrNull()?.let { id ->
                        serviceScope.launch {
                            musicRepository.updateLastPlayed(id)
                        }
                    }
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                lastPlayedManager.save()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                lastPlayedManager.save()
            }
        })

        // Restore state
        serviceScope.launch {
            val prefs = preferencesRepository.applicationPreferences.first()
            lastPlayedManager.restore { data, seed ->
                if (data != null) {
                    player.exoPlayer.setShuffleOrder(seed.toFactory()(data.startIndex, data.mediaItems.size, player))
                    player.setMediaItems(data.mediaItems, data.startIndex, data.startPositionMs)
                    player.prepare()
                    
                    if (prefs.autoplayOnLaunch) {
                        player.play()
                    }
                }
            }
        }
    }

    private inner class TuneoraSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                .add(COMMAND_FAVORITE)
                .add(COMMAND_SHUFFLE)
                .add(COMMAND_REPEAT)
                .add(COMMAND_STOP)
                .add(COMMAND_GET_LYRICS)
                .build()
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands,
                connectionResult.availablePlayerCommands
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                ACTION_FAVORITE -> {
                    val currentMediaId = player.currentMediaItem?.mediaId?.toLongOrNull()
                    if (currentMediaId != null) {
                        serviceScope.launch {
                            val song = musicRepository.getSongById(currentMediaId)
                            if (song != null) {
                                val newState = !song.isFavorite
                                musicRepository.toggleFavorite(currentMediaId, newState)
                                isCurrentTrackFavorite = newState
                                mediaSession?.let { updateNotification(it) }
                            }
                        }
                    }
                }
                ACTION_SHUFFLE -> {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                    mediaSession?.let { updateNotification(it) }
                }
                ACTION_REPEAT -> {
                    val nextMode = when (player.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                        else -> Player.REPEAT_MODE_OFF
                    }
                    player.repeatMode = nextMode
                    mediaSession?.let { updateNotification(it) }
                }
                ACTION_STOP -> {
                    player.pause()
                    player.stop()
                    stopSelf()
                }
                SERVICE_GET_LYRICS -> {
                    val mediaItem = player.currentMediaItem
                    if (mediaItem != null) {
                        val format = player.exoPlayer.audioFormat
                        val lyrics = lyricsRepository.getLyrics(mediaItem, format)
                        val bundle = Bundle().apply {
                            putParcelable("lyrics", lyrics)
                        }
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS, bundle))
                    }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private lateinit var notificationProvider: TuneoraNotificationProvider
    private var notificationCallback: MediaNotification.Provider.Callback? = null

    @UnstableApi
    private fun updateNotification(session: MediaSession) {
        val actionFactory = notificationProvider.lastActionFactory ?: return
        val callback = notificationCallback ?: return
        
        val notification = notificationProvider.createNotification(
            session,
            ImmutableList.of<CommandButton>(),
            actionFactory,
            callback
        )
        callback.onNotificationChanged(notification)
    }

    @UnstableApi
    private inner class TuneoraNotificationProvider : MediaNotification.Provider {
        private val defaultProvider = DefaultMediaNotificationProvider.Builder(this@TuneoraPlaybackService).build()
        var lastActionFactory: MediaNotification.ActionFactory? = null

        override fun createNotification(
            mediaSession: MediaSession,
            mediaButtonPreferences: ImmutableList<CommandButton>,
            actionFactory: MediaNotification.ActionFactory,
            onNotificationChangedCallback: MediaNotification.Provider.Callback
        ): MediaNotification {
            notificationCallback = onNotificationChangedCallback
            lastActionFactory = actionFactory
            val currentMediaId = player.currentMediaItem?.mediaId?.toLongOrNull()
            
            // Favorite icon logic (using cached state)
            val favoriteIcon = if (isCurrentTrackFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border

            val favoriteButton = CommandButton.Builder()
                .setSessionCommand(COMMAND_FAVORITE)
                .setIconResId(favoriteIcon)
                .setDisplayName("Favorite")
                .build()

            val stopButton = CommandButton.Builder()
                .setSessionCommand(COMMAND_STOP)
                .setIconResId(R.drawable.ic_close)
                .setDisplayName("Stop")
                .build()

            val customButtons = ImmutableList.of(
                favoriteButton,
                stopButton
            )

            return defaultProvider.createNotification(
                mediaSession,
                customButtons,
                actionFactory,
                onNotificationChangedCallback
            )
        }

        override fun handleCustomCommand(session: MediaSession, action: String, extras: Bundle): Boolean {
            return false
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val prefs = runBlocking { preferencesRepository.applicationPreferences.first() }
        if (prefs.stopOnDismiss || !player.isPlaying) {
            lastPlayedManager.save()
            player.pause()
            player.stop()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        lastPlayedManager.save()
        serviceScope.cancel()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
