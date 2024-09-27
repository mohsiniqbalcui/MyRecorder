package com.app.myrecorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun VoiceRecordingUI(
    duration: String = "0:13",
    speed: Float = 1.0f,
    isPlaying: Boolean = false,
    onPlayPauseClick: () -> Unit = {},
    onSpeedChangeClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFE0F7FA), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        IconButton(onClick = { onPlayPauseClick() }) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.baseline_pause_circle_filled_24 else R.drawable.baseline_play_circle_24
                ),
                contentDescription = "Play/Pause"
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform Display
        Waveform(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.width(8.dp))

        // Playback Speed
        Text(
            text = "x$speed",
            modifier = Modifier
                .background(Color(0xFF00ACC1), RoundedCornerShape(4.dp))
                .padding(4.dp),
            color = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(text = duration)
    }
}
