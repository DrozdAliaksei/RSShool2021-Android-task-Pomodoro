package com.example.rsshool2021_android_task_pomodoro.stopwatch.utils

interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long, isFinished: Boolean)

    fun reset(id: Int, startPeriod: Long, isStarted: Boolean)

    fun delete(id: Int)
}