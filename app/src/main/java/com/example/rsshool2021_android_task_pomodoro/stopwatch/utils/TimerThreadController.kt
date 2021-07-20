package com.example.rsshool2021_android_task_pomodoro.stopwatch.utils

interface TimerThreadController {

    suspend fun startTimer(id: Int)

    suspend fun stopTimer(id: Int, currentMs: Long)
}