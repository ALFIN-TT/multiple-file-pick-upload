package com.example.audiorecorderandimagepicker.audiorecorder

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.view.MotionEvent
import com.example.audiorecorderandimagepicker.R
import java.io.File
import java.io.IOException
import java.util.*


open class AudioRecorder(val context: Context) {

    lateinit var onAudioRecorderListener: OnAudioRecorderListener
    var isRecordingBeepSoundEnabled = false
    private var player: MediaPlayer? = null
    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    var minimumRecordingTime: Long = 1000 //minimum time for successful recording.
    var isEnableMinimumRecordingTime: Boolean =
        false// enable/disable minimum time for successful recording.
    private var RECORD_START: Int = R.raw.record_start
    private var RECORD_FINISHED: Int = R.raw.record_finished
    private var RECORD_ERROR: Int = R.raw.record_error

    //private val dir: File = File(context.Environment.getExternalStorageDirectory().absolutePath + "/soundrecorder/")
    //private val dir: File = File(context.cacheDir.absolutePath + "/pronunciation/")
    private val rootDirectory = context.cacheDir
    private val dir: File = File("$rootDirectory/pronunciation/")
    private var recordingTime: Long = 0
    private var timer = Timer()
    private var recordingTimeString: String = ""


    init {
        try {
            // create a File object for the parent directory
            //val recorderDirectory = File(context.cacheDir.absolutePath + "/pronunciation/")
            val recorderDirectory = File("$rootDirectory/pronunciation/")
            // have the object build the directory structure, if needed.
            recorderDirectory.mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (dir.exists()) {
            val count = dir.listFiles().size
            //output = Environment.getExternalStorageDirectory().absolutePath + "/soundrecorder/recording" + count + ".mp3"
            //output = context.cacheDir.absolutePath + "/pronunciation/recording" + 0 + ".mp3"
            output = "$rootDirectory/pronunciation/recording.m4a"

        }
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
    }

    @SuppressLint("RestrictedApi")
    fun startRecording() {
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            startTimer()
            onAudioRecorderListener.onAudioRecordingStarted()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            onAudioRecorderListener.onAudioRecordingError(e)
        } catch (e: IOException) {
            e.printStackTrace()
            onAudioRecorderListener.onAudioRecordingError(e)
        }

    }

    @SuppressLint("RestrictedApi")
    fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder?.release()
        onAudioRecorderListener.onAudioRecordingFinished(recordingTime)
        stopTimer()
        resetTimer()
        initRecorder()
    }


    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    fun pauseRecording() {
        stopTimer()
        mediaRecorder?.pause()
        onAudioRecorderListener.onAudioRecordingPaused()
    }

    @TargetApi(Build.VERSION_CODES.N)
    @SuppressLint("RestrictedApi")
    fun resumeRecording() {
        timer = Timer()
        startTimer()
        mediaRecorder?.resume()
        onAudioRecorderListener.onAudioRecordingResumed()

    }

    private fun initRecorder() {
        mediaRecorder = MediaRecorder()

        if (dir.exists()) {
            val count = dir.listFiles().size
            //output = Environment.getExternalStorageDirectory().absolutePath + "/soundrecorder/recording" + count + ".mp3"
            //output = context.cacheDir.absolutePath + "/pronunciation/recording" + 0 + ".mp3"
            output = "$rootDirectory/pronunciation/recording.m4a"

        }

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)
        val filePath = Uri.parse(output).getAudioPath(context)
        onAudioRecorderListener.onAudioRecorded(filePath, Uri.parse(output))
    }

    private fun startTimer() {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                recordingTime += 1
                updateDisplay()
            }
        }, 1000, 1000)
    }

    private fun stopTimer() {
        timer.cancel()
    }


    private fun resetTimer() {
        timer.cancel()
        recordingTime = 0
        recordingTimeString = "00:00"
    }

    private fun updateDisplay() {
        val minutes = recordingTime / (60)
        val seconds = recordingTime % 60
        val str = String.format("%d:%02d", minutes, seconds)
        recordingTimeString = str
        onAudioRecorderListener.onRecordingTimer(recordingTimeString, recordingTime)
    }


    fun onAudioButtonPressed(audioRecordButton: AudioRecordButton, motionEvent: MotionEvent?) {
        audioRecordButton.startScale()
        playBeepSound(audioRecordButton.context, RECORD_START) {
            startTime = System.currentTimeMillis()
            startRecording()
        }

    }

    fun onAudioButtonReleased(audioRecordButton: AudioRecordButton) {
        elapsedTime = System.currentTimeMillis() - startTime
        if (isEnableMinimumRecordingTime && isLessThanMinimumRecordingTime(elapsedTime)) {
            stopRecording()
            audioRecordButton.stopScale()
            playBeepSound(audioRecordButton.context, RECORD_ERROR) {}
            try {
                //deleteFile(context.cacheDir.absolutePath + "/pronunciation/recording" + 0 + ".mp3")
                deleteFile("$rootDirectory/pronunciation/recording.m4a")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onAudioRecorderListener.onAudioRecordingLessThanMinimumTime()
        } else {
            stopRecording()
            audioRecordButton.stopScale()
            playBeepSound(audioRecordButton.context!!, RECORD_FINISHED) {}
        }
    }

    private fun isLessThanMinimumRecordingTime(time: Long): Boolean {
        return time <= minimumRecordingTime
    }


    fun setCustomSounds(startSound: Int, finishedSound: Int, errorSound: Int) {
        //0 means do not play sound
        RECORD_START = startSound
        RECORD_FINISHED = finishedSound
        RECORD_ERROR = errorSound
    }


    private fun playBeepSound(
        context: Context, soundRes: Int,
        onSoundPlayed: () -> Unit
    ) {
        if (isRecordingBeepSoundEnabled) {
            if (soundRes == 0) return
            try {
                player = MediaPlayer()
                val afd: AssetFileDescriptor = context.resources.openRawResourceFd(soundRes)
                    ?: return
                player?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                player?.prepare()
                player?.start()
                player?.setOnCompletionListener { mp ->
                    mp.release()
                    onSoundPlayed.invoke()
                }
                player?.isLooping = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Suppress("DEPRECATION")
    fun playRecording(filePath: Uri) {
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

    fun stopPlaying(isDeleteFile: Boolean = true) {
        try {
            player?.stop()
            player?.reset()
            // if (isDeleteFile) deleteFile(context.cacheDir.absolutePath + "/pronunciation/recording" + 0 + ".mp3")
            if (isDeleteFile) deleteFile("$rootDirectory/pronunciation/recording.m4a")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}