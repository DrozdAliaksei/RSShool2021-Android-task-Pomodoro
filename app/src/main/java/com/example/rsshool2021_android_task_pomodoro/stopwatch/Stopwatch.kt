package com.example.rsshool2021_android_task_pomodoro.stopwatch

data class Stopwatch(
    val id: Int,
    var currentMs: Long,
    var isStarted: Boolean
)