package com.example.rsshool2021_android_task_pomodoro

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import com.example.rsshool2021_android_task_pomodoro.stopwatch.Stopwatch
import com.example.rsshool2021_android_task_pomodoro.stopwatch.StopwatchAdapter
import com.example.rsshool2021_android_task_pomodoro.stopwatch.utils.StopwatchListener
import com.example.rsshool2021_android_task_pomodoro.stopwatch.utils.TimerThreadController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(), StopwatchListener, TimerThreadController,
    LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var startTime = 0L
    private var timerJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startTime = System.currentTimeMillis()

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
                stopwatches.add(Stopwatch(nextId++, mills, mills, false))
                stopwatchAdapter.submitList(stopwatches.toList())
            } else Toast.makeText(this, "Choose timer period", Toast.LENGTH_LONG).show()
        }
    }

    override fun start(id: Int) {
        changeStopwatch(id, null, true)
    }

    override fun stop(id: Int, currentMs: Long) {
        changeStopwatch(id, currentMs, false)
    }

    override fun reset(id: Int, startPeriod: Long) {
        changeStopwatch(id, startPeriod, false)
    }

    override fun delete(id: Int) {
        stopwatches.remove(stopwatches.find { it.id == id })
        stopwatchAdapter.submitList(stopwatches.toList())
    }

    override suspend fun startTimer(id: Int) {
        timerJob = lifecycleScope.launch(Dispatchers.Main) {
            val stopwatch = stopwatches[id]
            val interval = UNIT_TEN_MS
            while (stopwatch.isStarted) {
                stopwatch.currentMs -= interval
                changeStopwatch(id, stopwatch.currentMs, true)
//                delay(UNIT_TEN_MS)
//                println("World!")
            }
        }
        TODO("Not yet implemented")
    }

    override suspend fun stopTimer(id: Int, currentMs: Long) {
        TODO("Not yet implemented")
    }

    private fun changeStopwatch(id: Int, currentMs: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Stopwatch>()
        stopwatches.forEach {
            if (it.id == id) {
                newTimers.add(
                    Stopwatch(
                        it.id,
                        it.startPeriod,
                        currentMs ?: it.currentMs,
                        isStarted
                    )
                )
            } else {
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
//    fun onAppBackgrounded() {
//        val startIntent = Intent(this, ForegroundService::class.java)
//        startIntent.putExtra(COMMAND_ID, COMMAND_START)
//        startIntent.putExtra(STARTED_TIMER_TIME_MS, startTime)
//        startService(startIntent)
//    }
//
//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
//    fun onAppForegrounded() {
//        val stopIntent = Intent(this, ForegroundService::class.java)
//        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
//        startService(stopIntent)
//    }
}