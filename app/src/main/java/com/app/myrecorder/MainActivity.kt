package com.app.myrecorder

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    /*
        @Composable
        fun AudioRecorderApp() {
    //         context = LocalContext.current
            val audioFilePath = remember { mutableStateOf<String?>(null) }
            val isRecording = remember { mutableStateOf(false) }
            val mediaRecorder = remember { mutableStateOf<MediaRecorder?>(null) }
            val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
            var isPlaying by remember { mutableStateOf(false) }


            var currentProgress by remember { mutableStateOf(0f) }  // Progress bar value
            var duration by remember { mutableStateOf(0f) }         // Total audio duration

            // Handler for updating the progress bar
            val handler = remember { Handler(Looper.getMainLooper()) }

            // Requesting audio recording permission launcher
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    if (!granted) {
                        Toast.makeText(context, "Audio permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            val hasPermission = remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }

            // Request permission at the start if not granted
            LaunchedEffect(Unit) {
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
                // Record Button
                Button(
                    onClick = {
                        if (hasPermission.value) {
                            startRecording(context, mediaRecorder, audioFilePath, handler, {
                                recordingProgress = it / 60000f  // Assuming 60 seconds max
                            })

                            isRecording.value = true
                        } else {
                            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isRecording.value
                ) {
                    Text("Start Recording")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stop Button
                Button(
                    onClick = {
                        stopRecording(context, mediaRecorder)
                        isRecording.value = false
                    },
                    enabled = isRecording.value
                ) {
                    Text("Stop Recording")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Button for Play and Stop Playback
                Button(
                    onClick = {
                        isPlaying = if (isPlaying) {
                            stopPlaying(context, mediaPlayer)
                            false
                        } else {
                            startPlaying(context, mediaPlayer, audioFilePath.value)
                            true
                        }
                    },
                    enabled = audioFilePath.value != null && !isRecording.value
                ) {
                    Text(if (isPlaying) "Stop Playing" else "Play Recording")
                }
            }

        }*/

    @Composable
    fun AudioRecorderWithPlayerAndProgress() {
        val context = LocalContext.current
        val audioFilePath = remember { mutableStateOf<String?>(null) }
        val isRecording = remember { mutableStateOf(false) }
        val mediaRecorder = remember { mutableStateOf<MediaRecorder?>(null) }
        val mediaPlayer = remember { mutableStateOf<MediaPlayer?>(null) }
        var isPlaying by remember { mutableStateOf(false) }
        var currentProgress by remember { mutableStateOf(0f) }  // Progress bar value for playback
        var recordingProgress by remember { mutableStateOf(0f) } // Progress bar value for recording
        var duration by remember { mutableStateOf(0f) }         // Total audio duration
        var startRecordingTime by remember { mutableStateOf(0L) } // Start time of recording

        // Handler for updating the progress bar
        val handler = remember { Handler(Looper.getMainLooper()) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Record Button
// Record Button
            Button(
                onClick = {
                    startRecording(context, mediaRecorder, audioFilePath, handler, {
                        val elapsedTime = System.currentTimeMillis() - startRecordingTime
                        recordingProgress = elapsedTime / 60000f  // Max duration is 60 seconds
                    }, { startRecordingTime = it })
                    isRecording.value = true
                },
                enabled = !isRecording.value
            ) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Recording Progress Bar (Slider)
            if (isRecording.value) {
                Slider(
                    value = recordingProgress,
                    onValueChange = {},
                    enabled = false, // Slider should not be draggable
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0f..1f
                )
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
                    if (isPlaying) {
                        stopPlaying(mediaPlayer, handler)
                        isPlaying = false
                    } else {
                        startPlaying(
                            context,
                            mediaPlayer,
                            audioFilePath.value,
                            handler,
                            { progress, total ->
                                currentProgress = progress / total
                                duration = total
                            })
                        isPlaying = true
                    }
                },
                enabled = audioFilePath.value != null && !isRecording.value
            ) {
                Text(if (isPlaying) "Stop Playing" else "Play Recording")
            }
        }
    }

    // Function to start playing audio
    fun startPlaying(
        context: Context,
        mediaPlayer: MutableState<MediaPlayer?>,
        filePath: String?
    ) {
        if (filePath == null) return

        mediaPlayer.value = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                Toast.makeText(context, "Playing audio", Toast.LENGTH_SHORT).show()

                setOnCompletionListener {
                    Toast.makeText(context, "Playback completed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Playback failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Function to stop playing audio
    fun stopPlaying(context: Context, mediaPlayer: MutableState<MediaPlayer?>) {
        mediaPlayer.value?.apply {
            if (isPlaying) {
                stop()
                reset()
                release()
                Toast.makeText(context, "Playback stopped", Toast.LENGTH_SHORT).show()
            }
        }
        mediaPlayer.value = null
    }

    /*
fun startRecording(
    context: Context,
    mediaRecorder: MutableState<MediaRecorder?>,
    audioFilePath: MutableState<String?>
) {
    val fileName = File(context.filesDir, "audio_record.3gp").absolutePath
    audioFilePath.value = fileName

    mediaRecorder.value = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)

        // Set output format to MPEG_4 for M4A
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)

        // Set the audio encoder to AAC for M4A
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        // Optionally, you can set a bitrate and sampling rate
        setAudioEncodingBitRate(128000) // 128 kbps
        setAudioSamplingRate(44100)     // 44.1 kHz
        setOutputFile(fileName)

        // Set max duration to 60 seconds (60,000 milliseconds)
        setMaxDuration(60000)  // 60 seconds

        // Notify the user when the time limit is reached
        setOnInfoListener { _, what, _ ->
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                Toast.makeText(
                    context,
                    "Recording stopped (60-second limit reached)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        try {
            prepare()
            start()
            Toast.makeText(
                context,
                "Recording started (will stop automatically after 60 seconds)",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Recording failed", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
*/

    //fun stopRecording(context: Context, mediaRecorder: MutableState<MediaRecorder?>) {
//    mediaRecorder.value?.apply {
//        stop()
//        release()
//        Toast.makeText(context, "Recording stopped", Toast.LENGTH_SHORT).show()
//    }
//    mediaRecorder.value = null
//}
    fun startRecording(
        context: android.content.Context,
        mediaRecorder: MutableState<MediaRecorder?>,
        audioFilePath: MutableState<String?>,
        handler: Handler,
        onRecordingProgress: (Float) -> Unit
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

                // Periodically update the recording progress
                handler.post(object : Runnable {
                    override fun run() {
                        onRecordingProgress(System.currentTimeMillis().toFloat())
                        handler.postDelayed(this, 100)
                    }
                })

                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Recording stopped", Toast.LENGTH_SHORT).show()
        }
        mediaRecorder.value = null
        handler.removeCallbacksAndMessages(null)  // Stop updating the recording progress
    }

    // Function to start playing audio and update playback progress
    fun startPlaying(
        context: android.content.Context,
        mediaPlayer: MutableState<MediaPlayer?>,
        filePath: String?,
        handler: Handler,
        onPlaybackProgress: (Float, Float) -> Unit
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
                            handler.postDelayed(this, 100)  // Update every 100ms
                        }
                    }
                })

                Toast.makeText(context, "Playing audio", Toast.LENGTH_SHORT).show()

                setOnCompletionListener {
                    Toast.makeText(context, "Playback completed", Toast.LENGTH_SHORT).show()
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

    fun playRecording(
        context: Context,
        mediaPlayer: MutableState<MediaPlayer?>,
        filePath: String?
    ) {
        if (filePath == null) return

        mediaPlayer.value = MediaPlayer().apply {
            try {
                setDataSource(filePath)
                prepare()
                start()
                Toast.makeText(context, "Playing audio", Toast.LENGTH_SHORT).show()

                setOnCompletionListener {
                    Toast.makeText(context, "Playback completed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Playback failed", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}