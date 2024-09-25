package com.app.myrecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.io.File
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioRecorderWithPlayerAndProgress()
        }
    }

    @Composable
    fun AudioRecorderWithPlayerAndProgress() {
        val context = LocalContext.current
        val audioFilePath = remember { mutableStateOf<String?>(null) }
        val isRecording = remember { mutableStateOf(false) }
        val mediaRecorder = remember { mutableStateOf<MediaRecorder?>(null) }
        val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
        var isAudioPlaying by remember { mutableStateOf(false) }
        var currentProgress by remember { mutableStateOf(0f) }  // Progress bar value for playback
        var recordingProgress by remember { mutableStateOf(0f) } // Progress bar value for recording
        var duration by remember { mutableStateOf(0f) }         // Total audio duration
        var startRecordingTime by remember { mutableStateOf(0L) } // Start time of recording
        val audioSamples = remember { mutableStateListOf<Float>() } // Audio samples for waveform
        var currentTimer by remember { mutableStateOf(0L) } // Timer in milliseconds

        // Handler for updating the progress bar
        val handler = remember { Handler(Looper.getMainLooper()) }

        // Timer effect that updates every second
        LaunchedEffect(isRecording.value) {
            if (isRecording.value) {
                currentTimer = 0L // Reset the timer when recording starts

                while (isRecording.value) {
                    delay(1000)
                    currentTimer += 1000
                }
            } else {
                currentTimer = 0L
            }
        }

        // Timer effect that updates every second
        LaunchedEffect(isAudioPlaying) {
            if (isAudioPlaying) {
                currentTimer = 0L // Reset the timer when recording starts
                while (isAudioPlaying) {
                    delay(1000)
                    currentTimer += 1000
                }
            } else {
                currentTimer = 0L
            }
        }

        val hasPermission = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        // Requesting audio recording permission launcher
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                if (!granted) {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }else{
                    hasPermission.value = true
                }
            }
        )


        // Ensure permission is requested when not granted
        LaunchedEffect(key1 = Unit) {
            if (!hasPermission.value) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
// Timer display
            Text(
                text = formatTime(currentTimer),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Record Button
            Button(
                onClick = {
                    if (hasPermission.value){
                        startRecording(context, mediaRecorder, audioFilePath, handler, {
                            val elapsedTime = System.currentTimeMillis() - startRecordingTime
                            recordingProgress = elapsedTime / 60000f  // Max duration is 60 seconds
                        }, { startRecordingTime = it })
                        isRecording.value = true

                    }else{
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = !isRecording.value

            ) {
//                Text(text = if (isRecording.value) "Stop Recording" else "Start Recording")
                Text("Start Recording")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stop Button
            Button(
                onClick = {
                    stopRecording(mediaRecorder, handler)
                    isRecording.value = false
                },
                enabled = isRecording.value
            ) {
                Text("Stop Recording")
            }

            /*Spacer(modifier = Modifier.height(16.dp))

            // Recording Progress Bar (Slider)
            if (isRecording.value) {
                Slider(
                    value = recordingProgress,
                    onValueChange = {},
                    enabled = false, // Slider should not be draggable
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0f..1f
                )
            }*/
            Spacer(modifier = Modifier.height(16.dp))

            // Waveform Visualization
            if (isRecording.value || isAudioPlaying) {
                WaveformVisualizer(audioSamples)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playback Progress Bar (Slider)
            if (duration > 0f) {
                Slider(
                    value = currentProgress,
                    onValueChange = { newValue ->
                        mediaPlayer.value?.seekTo((newValue * duration).toInt())  // Seek to new position
                        currentProgress = newValue
                    },
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0f..1f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Button for Play and Stop Playback
            Button(
                onClick = {
                    if (isAudioPlaying) {
                        stopPlaying(mediaPlayer, handler)
                        isAudioPlaying = false
                    } else {
                        startPlaying(
                            context,
                            mediaPlayer,
                            audioFilePath.value,
                            handler,
                            { progress, total ->
                                currentProgress = progress / total
                                duration = total
                            }, audioSamples, isAudioPlayingDone = {
                                isAudioPlaying = false
//                                stopPlaying(mediaPlayer, handler) ///
                            })
                        isAudioPlaying = true
                    }
                },
                enabled = audioFilePath.value != null && !isRecording.value
            ) {
                Text(if (isAudioPlaying) "Stop Playing" else "Play Recording")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Delete Button to delete the recording
            Button(
                onClick = {
                    deleteRecording(audioFilePath, context)
                    // Reset progress and states after deleting
                    currentProgress = 0f
                    recordingProgress = 0f
                    duration = 0f
                    isAudioPlaying = false
                    Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                },
                enabled = audioFilePath.value != null && !isRecording.value
            ) {
                Text("Delete Recording")
            }
        }
    }

    // Function to start recording audio
    fun startRecording(
        context: Context,
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

            setMaxDuration(60000)  // 60 seconds

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
//            Toast.makeText(context, "Recording stopped", Toast.LENGTH_SHORT).show()
        }
        mediaRecorder.value = null
        handler.removeCallbacksAndMessages(null)  // Stop updating the recording progress
    }

    // Function to start playing audio and update playback progress
    fun startPlaying(
        context: Context,
        mediaPlayer: MutableState<MediaPlayer?>,
        filePath: String?,
        handler: Handler,
        onPlaybackProgress: (Float, Float) -> Unit,
        audioSamples: MutableList<Float>,
        isAudioPlayingDone: (Boolean) -> Unit
    ) {
        if (filePath == null) return

        mediaPlayer.value = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()

                val durationMs = duration.toFloat()

                // Periodically update the playback progress
                handler.post(object : Runnable {
                    override fun run() {
                        if (isPlaying) {
                            val currentPosition = currentPosition.toFloat()
                            onPlaybackProgress(currentPosition, durationMs)

                            // Simulate audio samples for waveform visualization
                            audioSamples.clear()
                            for (i in 0 until 100) {
                                audioSamples.add((Math.random().toFloat() - 0.5f) * 2) // Random amplitude
                            }

                            handler.postDelayed(this, 100)  // Update every 100ms
                        }
                    }
                })

//                Toast.makeText(context, "Playing audio", Toast.LENGTH_SHORT).show()

                setOnCompletionListener {
                    Toast.makeText(context, "Playback completed", Toast.LENGTH_SHORT).show()
                    isAudioPlayingDone(false)
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Playback failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Function to stop playing audio and stop progress updates
    fun stopPlaying(mediaPlayer: MutableState<MediaPlayer?>, handler: Handler) {
        mediaPlayer.value?.apply {
            if (isPlaying) {
                stop()
                reset()
                release()
                Toast.makeText(context, "Playback stopped", Toast.LENGTH_SHORT).show()
            }
        }
        mediaPlayer.value = null
        handler.removeCallbacksAndMessages(null)  // Stop updating the playback progress
    }

    // Function to delete the recording
    fun deleteRecording(audioFilePath: MutableState<String?>, context: Context) {
        audioFilePath.value?.let {
            val file = File(it)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    audioFilePath.value = null
                    Toast.makeText(context, "Recording deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete recording", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}