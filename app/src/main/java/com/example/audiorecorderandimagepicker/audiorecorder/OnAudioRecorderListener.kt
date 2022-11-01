package com.example.audiorecorderandimagepicker.audiorecorder

import android.net.Uri

interface OnAudioRecorderListener {
    fun onAudioRecordingStarted() {}
    fun onAudioRecordingCancel() {}
    fun onAudioRecordingFinished(recordTime: Long) {}
    fun onAudioRecordingPaused() {}
    fun onAudioRecordingResumed() {}
    fun onAudioRecordingError(error: Exception) {}
    fun onAudioRecordingLessThanMinimumTime() {}
    fun onRecordingTimer(formattedTime: String, time: Long) {}
    fun onAudioRecorded(string: String?, uri: Uri)
}