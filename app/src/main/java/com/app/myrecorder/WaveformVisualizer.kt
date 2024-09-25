package com.app.myrecorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(audioSamples: List<Float>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val path = Path()
        val widthPerSample = size.width / (audioSamples.size.coerceAtLeast(1))
        var xPos = 0f

        for (sample in audioSamples) {
            val amplitude = sample * size.height / 2
            path.moveTo(xPos, size.height / 2)
            path.lineTo(xPos, size.height / 2 - amplitude)
            xPos += widthPerSample
        }

        drawPath(path, color = Color.Green)
    }
}