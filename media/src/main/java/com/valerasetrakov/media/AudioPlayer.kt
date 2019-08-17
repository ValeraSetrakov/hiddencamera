package com.valerasetrakov.media

import android.media.AudioAttributes
import android.media.MediaPlayer

object AudioPlayer {

    private var mediaPlayer: MediaPlayer? = null

    fun startPlay(audio: String) {
        stopPlay()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(AudioAttributes.Builder().build())
            setDataSource(audio)
            setOnPreparedListener {
                start()
            }
            setOnCompletionListener {
                stopPlay()
            }
            prepareAsync()
        }
    }

    fun stopPlay() {
        mediaPlayer?.run {
            reset()
            release()
            mediaPlayer = null
        }
    }
}