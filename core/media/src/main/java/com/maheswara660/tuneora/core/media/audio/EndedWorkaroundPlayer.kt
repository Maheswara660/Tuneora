package com.maheswara660.tuneora.core.media.audio

import androidx.media3.common.DeviceInfo
import androidx.media3.common.ForwardingSimpleBasePlayer
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.exoplayer.ExoPlayer

class EndedWorkaroundPlayer(exoPlayer: ExoPlayer) : ForwardingSimpleBasePlayer(exoPlayer),
    Player.Listener {

    companion object {
        private const val TAG = "EndedWorkaroundPlayer"
    }

    private val remoteDeviceInfo = DeviceInfo.Builder(DeviceInfo.PLAYBACK_TYPE_REMOTE).build()

    init {
        player.addListener(this)
    }

    val exoPlayer
        get() = player as ExoPlayer

    val shuffleOrder: androidx.media3.exoplayer.source.ShuffleOrder
        get() = try {
            // Use reflection to access the internal shuffleOrder of ExoPlayerImpl
            val method = player.javaClass.getMethod("getShuffleOrder")
            method.invoke(player) as androidx.media3.exoplayer.source.ShuffleOrder
        } catch (e: Exception) {
            try {
                val field = player.javaClass.getDeclaredField("shuffleOrder")
                field.isAccessible = true
                field.get(player) as androidx.media3.exoplayer.source.ShuffleOrder
            } catch (e2: Exception) {
                androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder(0)
            }
        }

    var nextShuffleOrder:
            ((firstIndex: Int, mediaItemCount: Int, EndedWorkaroundPlayer) -> CircularShuffleOrder)? =
        null
    var isEnded = false
        set(value) {
            Log.d(TAG, "isEnded set to $value (was $field)")
            field = value
        }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            isEnded = false
        }
    }

    override fun getState(): State {
        if (isEnded) {
            val superState = super.getState()
            if (superState.playerError != null) {
                isEnded = false
                return superState
            }
            return superState.buildUpon().setPlaybackState(Player.STATE_ENDED).setIsLoading(false).build()
        }
        if (player.currentTimeline.isEmpty) {
            return super.getState().buildUpon().setDeviceInfo(remoteDeviceInfo).build()
        }
        return super.getState()
    }
}
