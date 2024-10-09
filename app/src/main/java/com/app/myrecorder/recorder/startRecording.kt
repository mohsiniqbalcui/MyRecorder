package com.app.myrecorder.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Handler
import android.widget.Toast
import androidx.compose.runtime.MutableState
import java.io.File
import java.io.IOException

// Function to start recording audio
     fun startRecording(
    context: Context,
    maxRecordingTime: Int,
    mediaRecorder: MutableState<MediaRecorder?>,
    audioFilePath: MutableState<String?>,
    handler: Handler,
    onRecordingProgress: (Float) -> Unit,
    onRecordingStart: (Long) -> Unit
    ) {
        val fileName = File(context.filesDir, "audio_record.m4a").absolutePath
        audioFilePath.value = fileName

        mediaRecorder.value = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)  // 128 kbps for good quality
            setAudioSamplingRate(44100)      // 44.1 kHz sample rate
            setOutputFile(fileName)

            setMaxDuration(maxRecordingTime)  // 60 seconds

            try {
                prepare()
                start()

                val startTime = System.currentTimeMillis()
                onRecordingStart(startTime)

                // Periodically update the recording progress
                handler.post(object : Runnable {
                    override fun run() {
                        onRecordingProgress((System.currentTimeMillis() - startTime).toFloat())
                        handler.postDelayed(this, 100)
                    }
                })

//                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Toast.makeText(context, "Recording failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }


     fun stopRecording(mediaRecorder: MutableState<MediaRecorder?>, handler: Handler) {
        mediaRecorder.value?.apply {
            stop()
            release()
        }
        mediaRecorder.value = null
        handler.removeCallbacksAndMessages(null)  // Stop updating the recording progress
    }

// Function to delete the recording
fun deleteRecording(audioFilePath: MutableState<String?>, context: Context) {
    audioFilePath.value?.let {
        val file = File(it)

        if (file.exists()) {
            val deleted = file.delete()
            if (deleted) {
                audioFilePath.value = null
//                    Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to delete recording", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
