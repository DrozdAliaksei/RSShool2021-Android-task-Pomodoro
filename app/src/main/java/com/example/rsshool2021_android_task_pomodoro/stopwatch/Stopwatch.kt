package com.example.rsshool2021_android_task_pomodoro.stopwatch

data class Stopwatch(
    val id: Int,
    val startPeriod: Long,
    var currentMs: Long,
    var isStarted: Boolean,
    var isFinished: Boolean
)
