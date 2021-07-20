package com.example.rsshool2021_android_task_pomodoro.stopwatch.utils

interface StopwatchListener {

    fun start(id: Int)

    fun stop(id: Int, currentMs: Long)

    fun reset(id: Int, startPeriod: Long)

    fun delete(id: Int)
}