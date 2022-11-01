package com.example.audiorecorderandimagepicker.audiorecorder.record

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import com.example.audiorecorderandimagepicker.audiorecorder.toast

class AudioPlayer(val context: Context) {

    var player: MediaPlayer? = null

    @Suppress("DEPRECATION")
    fun play(filePath: Uri) {
        val manager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (manager.isMusicActive) {
            context.toast("Another recording is just playing! Wait until it's finished!")
        } else {
            player = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                setDataSource(context, filePath)
                prepare()
                start()
            }
        }
    }

    fun stop() {
        try {
            player?.stop()
            player?.reset()
            player?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}