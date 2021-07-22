package com.example.rsshool2021_android_task_pomodoro

import android.app.TimePickerDialog
import android.content.Intent
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
//    private var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        startTime = System.currentTimeMillis()

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
                if (stopwatches.size <= 5) {
                    stopwatches.add(Stopwatch(nextId++, mills, mills, false))
                    stopwatchAdapter.submitList(stopwatches.toList())
                } else Toast.makeText(this, "Too many deadlines", Toast.LENGTH_LONG).show()
            } else Toast.makeText(this, "Choose timer period", Toast.LENGTH_LONG).show()
        }

//        lifecycleScope.launch(Dispatchers.Main) {
//            stopwatches.forEach {
//                if (it.isStarted) {
//                    val interval = System.currentTimeMillis() - startTime
//                    it.currentMs -= interval
//                    changeStopwatch(it.id, it.currentMs, true)
//                }
//            }
//        }
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
                newTimers.add(
                    Stopwatch(
                        it.id,
                        it.startPeriod,
                        it.currentMs,
                        false
                    )
                )
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        val runningTimer = stopwatches.find { it.isStarted }?.let { stopwatches[it.id] }

        if (runningTimer != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(
                STARTED_TIMER_TIME_MS,
                runningTimer.currentMs
            ) //startTime runningTimer.currentMs
            startIntent.putExtra(SYSTEM_TIME, System.currentTimeMillis())
            startService(startIntent)
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        try {
            val stopIntent = Intent(this, ForegroundService::class.java)
            stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
            startService(stopIntent)
        } catch (ex: Exception){
            Log.i("exception", ex.stackTraceToString())
        }
    }
}