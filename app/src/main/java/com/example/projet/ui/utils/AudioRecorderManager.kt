package com.example.projet.ui.utils

import android.content.Context
import android.media.MediaRecorder
import android.media.MediaPlayer
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isRecording = false
    private var recordingFilePath: String? = null

    fun startRecording(fileName: String = "voice_record_${System.currentTimeMillis()}.3gp"): Boolean {
        return try {
            val recordingFile = File(context.cacheDir, fileName)
            recordingFilePath = recordingFile.absolutePath

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(recordingFilePath)
                prepare()
                start()
            }
            isRecording = true
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun stopRecording(): Long {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val file = File(recordingFilePath ?: return 0)
            file.length()
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
    }

    fun playRecording(filePath: String, onComplete: () -> Unit = {}) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener { onComplete() }
                start()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun getCurrentPosition(): Int = try {
        mediaPlayer?.currentPosition ?: 0
    } catch (e: IllegalStateException) { 0 }

    fun stopPlayback() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    fun isRecordingInProgress(): Boolean = isRecording

    fun getRecordingFilePath(): String? = recordingFilePath

    fun cleanup() {
        stopPlayback()
        if (isRecording) {
            stopRecording()
        }
    }
}

