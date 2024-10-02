package com.app.myrecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.app.myrecorder.recorder.deleteRecording
import com.app.myrecorder.recorder.startRecording
import com.app.myrecorder.recorder.stopRecording
import com.linc.audiowaveform.AudioWaveform
import com.linc.audiowaveform.model.AmplitudeType
import com.linc.audiowaveform.model.WaveformAlignment
import kotlinx.coroutines.delay
import linc.com.amplituda.Amplituda
import linc.com.amplituda.Cache
import linc.com.amplituda.callback.AmplitudaErrorListener
import java.io.IOException

class MainActivity : ComponentActivity() {
    private var amplitudes: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    private val context = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioRecorderWithPlayerAndProgress()
        }
    }

    @Composable
    fun AudioRecorderWithPlayerAndProgress() {
        var amplituda: Amplituda = Amplituda(LocalContext.current)

        val context = LocalContext.current
        val audioFilePath = remember { mutableStateOf<String?>(null) }
        var isRecording by remember { mutableStateOf(false) }
        var iswaveFormReady by remember { mutableStateOf(false) }
        val mediaRecorder = remember { mutableStateOf<MediaRecorder?>(null) }
        val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
        var isAudioPlaying by remember { mutableStateOf(false) }
        var currentPlayerProgress by remember { mutableStateOf(0f) }  // Progress bar value for playback
        var recordingProgress by remember { mutableStateOf(0f) } // Progress bar value for recording
        var duration by remember { mutableStateOf(0f) }         // Total audio duration
        var startRecordingTime by remember { mutableStateOf(0L) } // Start time of recording
        val audioSamples = remember { mutableStateListOf<Float>() } // Audio samples for waveform
        var currentTimer by remember { mutableStateOf(0L) } // Timer in milliseconds
        var maxRecordingTime by remember { mutableStateOf(720000) } // Timer in milliseconds

        // Handler for updating the progress bar
        val handler = remember { Handler(Looper.getMainLooper()) }

        // Timer effect that updates every second
// Timer effect that updates every second for recording
        LaunchedEffect(isRecording) {
            if (isRecording) {
                currentTimer = 0L // Reset the timer when recording starts
                while (isRecording && currentTimer < maxRecordingTime) { // Ensure the timer stops at 60 seconds
                    delay(1000)
                    currentTimer += 1000
                    if (currentTimer >= maxRecordingTime) {
                        isRecording = false
                        iswaveFormReady = true

                        break // Break out of the loop when 60 seconds are reached
                    }
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
                } else {
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
                    if (hasPermission.value) {
                        iswaveFormReady = false
                        startRecording(
                            context,
                            maxRecordingTime,
                            mediaRecorder,
                            audioFilePath,
                            handler,
                            {
                                val elapsedTime = System.currentTimeMillis() - startRecordingTime
                                recordingProgress =
                                    elapsedTime / 60000f  // Max duration is 60 seconds
                            },
                            { startRecordingTime = it })
                        isRecording = true

                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                enabled = !isRecording

            ) {
//                Text(text = if (isRecording.value) "Stop Recording" else "Start Recording")
                Text("Start Recording")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stop Button
            Button(
                onClick = {
                    stopRecording(mediaRecorder, handler)
                    isRecording = false
                    iswaveFormReady = true
                },
                enabled = isRecording
            ) {
                Text("Stop Recording")
            }

            Spacer(modifier = Modifier.height(16.dp))

//            // Recording Progress Bar (Slider)
//            if (isRecording) {
//                Slider(
//                    value = recordingProgress,
//                    onValueChange = {},
//                    enabled = false, // Slider should not be draggable
//                    modifier = Modifier.fillMaxWidth(),
//                    valueRange = 0f..1f
//                )
//            }
//            Spacer(modifier = Modifier.height(16.dp))

//            DisplayAudioWaveform(audioSamples, context, handler, mediaPlayer, isAudioPlaying, waveformProgress, amplituda)

            if (iswaveFormReady) {

                amplitudes =
                    amplituda.processAudio(audioFilePath.value, Cache.withParams(Cache.REFRESH))
                        .get(AmplitudaErrorListener {
                            it.printStackTrace()
                        })
                        .amplitudesAsList()

                AudioWaveform(
                    modifier = Modifier.fillMaxWidth(),
                    // Spike DrawStyle: Fill or Stroke
                    style = Fill,
                    waveformAlignment = WaveformAlignment.Center,
                    amplitudeType = AmplitudeType.Avg,
                    // Colors could be updated with Brush API
                    progressBrush = SolidColor(Color.Magenta),
                    waveformBrush = SolidColor(Color.LightGray),
                    spikeWidth = 4.dp,
                    spikePadding = 2.dp,
                    spikeRadius = 4.dp,
                    progress = currentPlayerProgress,
                    amplitudes = amplitudes,
                    onProgressChange = {
                        currentPlayerProgress = it
                        mediaPlayer.value?.seekTo((it * duration).toInt())  // Seek to new position

                    },
                    onProgressChangeFinished = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            /*
                        // Playback Progress Bar (Slider)
                        if (duration > 0f) {
                            Slider(
                                value = currentPlayerProgress,
                                onValueChange = { newValue ->
                                    mediaPlayer.value?.seekTo((newValue * duration).toInt())  // Seek to new position
                                    currentPlayerProgress = newValue
                                },
                                modifier = Modifier.fillMaxWidth(),
                                valueRange = 0f..1f
                            )
                        }
            */

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle Button for Play and Stop Playback
            Button(
                onClick = {
                    if (isAudioPlaying) {
//                        stopPlaying(mediaPlayer, handler)
                        currentPlayerProgress = mediaPlayer.value?.currentPosition?.toFloat() ?: 0f
                        Log.e("TAG", "onClick: $currentPlayerProgress")
                        pausePlaying(mediaPlayer)
                        isAudioPlaying = false

                    } else {
                        startPlaying(
                            context,
                            currentPlayerProgress = currentPlayerProgress.toInt(),
                            mediaPlayer = mediaPlayer,
                            filePath = audioFilePath.value,
                            handler = handler,
                            onPlaybackProgress = { progress, total ->
                                currentPlayerProgress = progress / total
                                duration = total
                            }, isAudioPlayingDone = {
                                isAudioPlaying = false
                                stopPlaying(mediaPlayer, handler)
                            }
                        )
                        isAudioPlaying = true
                    }
                },
                enabled = audioFilePath.value != null && !isRecording
            ) {
                Text(if (isAudioPlaying) "Pause Playing" else "Play Recording")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Delete Button to delete the recording
            Button(
                onClick = {
                    iswaveFormReady = false
                    stopPlaying(mediaPlayer, handler)

                    deleteRecording(audioFilePath, context)

                    // Reset progress and states after deleting
                    currentPlayerProgress = 0f
                    recordingProgress = 0f
                    duration = 0f
                    isAudioPlaying = false
                },
                enabled = audioFilePath.value != null && !isRecording
            ) {
                Text("Delete Recording")
            }
        }
    }


    // Function to start playing audio and update playback progress
    private fun startPlaying(
        context: Context,
        currentPlayerProgress: Int,
        mediaPlayer: MutableState<MediaPlayer?>,
        filePath: String?,
        handler: Handler,
        onPlaybackProgress: (Float, Float) -> Unit,
        isAudioPlayingDone: (Boolean) -> Unit
    ) {
        if (filePath == null) return

        mediaPlayer.value = MediaPlayer().apply {
            try {
                setDataSource(filePath)

                if (!isPlaying && currentPlayerProgress > 0) {
                    Log.e("TAG", "onClick resume: $currentPlayerProgress")
                    mediaPlayer.value?.seekTo((currentPlayerProgress))  // Seek to new position
                } else {
                    prepare()
                    mediaPlayer.value?.seekTo(0)  // Seek to new position

                }
                start()

                val durationMs = duration.toFloat()

                // Periodically update the playback progress
                handler.post(object : Runnable {
                    override fun run() {
                        if (isPlaying) {
                            val currentPosition = currentPlayerProgress.toFloat()
                            onPlaybackProgress(currentPosition, durationMs)

                            handler.postDelayed(this, 100)  // Update every 100ms
                        }
                    }
                })

                setOnCompletionListener {
                    isAudioPlayingDone(false)
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Playback failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Function to stop playing audio and stop progress updates
    private fun stopPlaying(mediaPlayer: MutableState<MediaPlayer?>, handler: Handler) {
        mediaPlayer.value?.apply {
            if (isPlaying) {
                stop()
                reset()
                release()
//                Toast.makeText(context, "Playback stopped", Toast.LENGTH_SHORT).show()
            }
        }
        mediaPlayer.value = null
        handler.removeCallbacksAndMessages(null)  // Stop updating the playback progress
    }

    // Function to stop playing audio and stop progress updates
    private fun pausePlaying(mediaPlayer: MutableState<MediaPlayer?>) {
        mediaPlayer.value?.apply {
            if (isPlaying) {
                pause()  // Pause the playback
            }
        }
    }

}