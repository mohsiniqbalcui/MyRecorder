package com.app.myrecorder

// Helper function to format the timer as mm:ss
fun formatTime(timeMillis: Long): String {
    val minutes = (timeMillis / 1000) / 60
    val seconds = (timeMillis / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}