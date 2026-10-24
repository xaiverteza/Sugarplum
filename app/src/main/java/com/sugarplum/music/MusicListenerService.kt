
package com.sugarplum.music

import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import java.io.File
import java.io.FileOutputStream

class MusicListenerService : NotificationListenerService() {

    companion object {
        var instance: MusicListenerService? = null
        var spotifyController: MediaController? = null

        const val PREFS = "sugarplum_music"
        const val UPDATE = "com.sugarplum.music.UPDATE"
        const val PLAY_PAUSE = "com.sugarplum.music.PLAY_PAUSE"
        const val NEXT = "com.sugarplum.music.NEXT"
        const val PREVIOUS = "com.sugarplum.music.PREVIOUS"

        fun control(action: String) {
            val controller = spotifyController
            if (controller == null) return

            when (action) {
                PLAY_PAUSE -> {
                    val playing = controller.playbackState?.state ==
                        PlaybackState.STATE_PLAYING
                    if (playing) controller.transportControls.pause()
                    else controller.transportControls.play()
                }
                NEXT -> controller.transportControls.skipToNext()
                PREVIOUS -> controller.transportControls.skipToPrevious()
            }
        }
    }

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            saveMetadata(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            saveState(state)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        refreshSpotifySession()
    }

    override fun onListenerDisconnected() {
        spotifyController?.unregisterCallback(callback)
        spotifyController = null
        instance = null
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(
        sbn: android.service.notification.StatusBarNotification?
    ) {
        if (sbn?.packageName == "com.spotify.music") {
            refreshSpotifySession()
        }
    }

    override fun onNotificationRemoved(
        sbn: android.service.notification.StatusBarNotification?
    ) {
        if (sbn?.packageName == "com.spotify.music") {
            refreshSpotifySession()
        }
    }

    private fun refreshSpotifySession() {
        try {
            val sessions = getSystemService(
                android.media.session.MediaSessionManager::class.java
            ).getActiveSessions(
                ComponentName(this, MusicListenerService::class.java)
            )

            val found = sessions.firstOrNull {
                it.packageName == "com.spotify.music"
            }

            spotifyController?.unregisterCallback(callback)
            spotifyController = found

            if (found != null) {
                found.registerCallback(callback)
                saveMetadata(found.metadata)
                saveState(found.playbackState)
            }
        } catch (_: SecurityException) {
            // Notification access may have been disabled.
        } catch (_: Exception) {
            // Spotify may not have an active media session yet.
        }
    }

    private fun saveMetadata(metadata: MediaMetadata?) {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: "Nothing playing"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: "Play something on Spotify"

        prefs.edit()
            .putString("title", title)
            .putString("artist", artist)
            .apply()

        val art = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)

        if (art != null) {
            try {
                val file = File(filesDir, "sugarplum_art.png")
                FileOutputStream(file).use {
                    art.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                prefs.edit().putBoolean("has_art", true).apply()
            } catch (_: Exception) {
                prefs.edit().putBoolean("has_art", false).apply()
            }
        } else {
            prefs.edit().putBoolean("has_art", false).apply()
        }

        refreshWidgets()
    }

    private fun saveState(state: PlaybackState?) {
        val playing = state?.state == PlaybackState.STATE_PLAYING
        getSharedPreferences(PREFS, MODE_PRIVATE)
            .edit().putBoolean("playing", playing).apply()
        refreshWidgets()
    }

    private fun refreshWidgets() {
        sendBroadcast(Intent(UPDATE).setPackage(packageName))
        SmallMusicWidget.refresh(this)
        LargeMusicWidget.refresh(this)
    }
}
