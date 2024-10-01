package com.app.myrecorder

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import kotlin.random.Random

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

@Composable
fun Waveform(modifier: Modifier = Modifier) {
    val waveformValues = remember { generateRandomWaveform() }

    // Define bar width and spacing
    val barWidth = 6.dp
    val barSpacing = 4.dp

    // Canvas to display the waveform
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        waveformValues.forEachIndexed { index, value ->
            // Bar height relative to the canvas height
            val barHeight = canvasHeight * value

            // Draw a bar for each value in waveformValues
            drawRect(
                color = Color(0xFF00ACC1),
                topLeft = androidx.compose.ui.geometry.Offset(
                    x = (index * (barWidth.toPx() + barSpacing.toPx())),
                    y = canvasHeight - barHeight
                ),
                size = androidx.compose.ui.geometry.Size(
                    width = barWidth.toPx(),
                    height = barHeight
                )
            )
        }
    }
}

fun generateRandomWaveform(): List<Float> {
    // Random values between 0.3 and 1.0 to ensure visible bars
    return List(70) { Random.nextFloat().coerceIn(0.3f, 1.0f) }
}