package com.example.rsshool2021_android_task_pomodoro

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import com.example.rsshool2021_android_task_pomodoro.stopwatch.Stopwatch
import com.example.rsshool2021_android_task_pomodoro.stopwatch.StopwatchAdapter
import com.example.rsshool2021_android_task_pomodoro.stopwatch.utils.StopwatchListener
import java.util.*

class MainActivity : AppCompatActivity(), StopwatchListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = stopwatchAdapter
        }

        binding.timePicker.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR)
            val minute = c.get(Calendar.MINUTE)
            val timePickerDialog =
                TimePickerDialog(this, { _, h, m ->
                    binding.timePicker.text = resources.getString(R.string.time, h, m)
                }, hour, minute, true)

            timePickerDialog.show()
        }

        binding.addNewStopwatchButton.setOnClickListener {
            if (binding.timePicker.text.isNotEmpty()) {
                val time = binding.timePicker.text.trim()
                val h = time.subSequence(0, time.indexOf(":")).toString()
                val m = time.subSequence(time.indexOf(":") + 1, time.length).toString()
                val mills = (h.toInt() * 60L + m.toInt()) * 60L * 1000L
                if (mills == 0L) {
                    Toast.makeText(this, "Timer need to be at least one minute", Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }
                if (stopwatches.size <= 10) {
                    stopwatches.add(
                        Stopwatch(
                            id = nextId++,
                            startPeriod = mills,
                            currentMs = mills,
                            isStarted = false,
                            isFinished = false
                        )
                    )
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else Toast.makeText(this, "Too many deadlines", Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, "Choose timer period", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onAppForegrounded()
    }

    override fun start(id: Int) {
        changeStopwatch(
            id = id,
            currentMs = null,
            isStarted = true,
            isFinished = false
        )
    }

    override fun stop(id: Int, currentMs: Long, isFinished: Boolean) {
        if (isFinished) Toast.makeText(this, "Time is out!!!", Toast.LENGTH_LONG).show()
        changeStopwatch(
            id = id,
            currentMs = currentMs,
            isStarted = false,
            isFinished = isFinished
        )
    }

    override fun reset(id: Int, startPeriod: Long, isStarted: Boolean) {
        changeStopwatch(
            id = id,
            currentMs = startPeriod,
            isStarted = isStarted,
            isFinished = false
        )
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    private fun changeStopwatch(
        id: Int,
        currentMs: Long?,
        isStarted: Boolean,
        isFinished: Boolean,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopwatches.replaceAll {
                when {
                    it.id == id -> Stopwatch(
                        id = it.id,
                        startPeriod = it.startPeriod,
                        currentMs = currentMs ?: it.currentMs,
                        isStarted = isStarted,
                        isFinished = isFinished
                    )
                    it.id != id && it.isStarted ->
                        Stopwatch(
                            id = it.id,
                            startPeriod = it.startPeriod,
                            currentMs = currentMs ?: it.currentMs,
                            isStarted = false,
                            isFinished = isFinished
                        )
                    else -> it
                }
            }
        }
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        try {
            val runningTimer = stopwatches.find { it.isStarted }
            if (runningTimer != null) {
                val startIntent = Intent(this, ForegroundService::class.java)
                startIntent.putExtra(COMMAND_ID, COMMAND_START)
                startIntent.putExtra(
                    STARTED_TIMER_TIME_MS,
                    runningTimer.currentMs
                )
                startIntent.putExtra(SYSTEM_TIME, System.currentTimeMillis())
                startService(startIntent)
            }
        } catch (ex: Exception) {
            Log.i("exception", "onAppBackgrounded start exception: ${ex.stackTraceToString()}")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        try {
            val stopIntent = Intent(this, ForegroundService::class.java)
            stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
            startService(stopIntent)
        } catch (ex: Exception) {
            Log.i("exception", "onAppForegrounded start exception: ${ex.stackTraceToString()}")
        }
    }
}