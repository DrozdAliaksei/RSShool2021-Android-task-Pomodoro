package com.example.rsshool2021_android_task_pomodoro

import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rsshool2021_android_task_pomodoro.databinding.ActivityMainBinding
import com.example.rsshool2021_android_task_pomodoro.stopwatch.Stopwatch
import com.example.rsshool2021_android_task_pomodoro.stopwatch.StopwatchAdapter
import com.example.rsshool2021_android_task_pomodoro.stopwatch.utils.StopwatchListener
import java.util.*

class MainActivity : AppCompatActivity(), StopwatchListener {

    private lateinit var binding: ActivityMainBinding

    private val stopwatchAdapter = StopwatchAdapter(this)
    private val stopwatches = mutableListOf<Stopwatch>()
    private var nextId = 0
    private var startTime = 0L

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

    override fun reset(id: Int) {
        changeStopwatch(id, 0L, false)
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
                newTimers.add(it)
            }
        }
        stopwatchAdapter.submitList(newTimers)
        stopwatches.clear()
        stopwatches.addAll(newTimers)
    }
}