package io.github.zyrouge.symphony.services.radio

import android.media.AudioManager
import androidx.media.AudioAttributesCompat
import androidx.media.AudioFocusRequestCompat
import androidx.media.AudioManagerCompat
import io.github.zyrouge.symphony.Symphony

// Credits: https://github.com/RetroMusicPlayer/RetroMusicPlayer/blob/7b1593009319c8d8e04660470ba37f814e8203eb/app/src/main/java/code/name/monkey/retromusic/service/LocalPlayback.kt
class RadioFocus(val symphony: Symphony) {
    val audioManager = symphony.applicationContext.getSystemService(AudioManager::class.java)
    val audioFocusRequest = AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributesCompat.Builder()
                .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setOnAudioFocusChangeListener { event ->
            when (event) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    when {
                        symphony.radio.isPlaying -> symphony.radio.restoreVolume()
                        else -> symphony.radio.resume()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (!symphony.settings.getIgnoreAudioFocusLoss()) {
                        symphony.radio.pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    if (symphony.radio.isPlaying) {
                        symphony.radio.duck()
                    }
                }
            }
        }
        .build()

    fun requestFocus() = AudioManagerCompat.requestAudioFocus(
        audioManager,
        audioFocusRequest
    ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED

    fun abandonFocus() =
        AudioManagerCompat.abandonAudioFocusRequest(
            audioManager,
            audioFocusRequest
        ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
}