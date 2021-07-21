package com.example.rsshool2021_android_task_pomodoro.stopwatch

import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.rsshool2021_android_task_pomodoro.R
import com.example.rsshool2021_android_task_pomodoro.UNIT_TEN_MS
import com.example.rsshool2021_android_task_pomodoro.databinding.StopwatchItemBinding
import com.example.rsshool2021_android_task_pomodoro.displayTime
import com.example.rsshool2021_android_task_pomodoro.stopwatch.utils.StopwatchListener

class StopwatchViewHolder(
    private val binding: StopwatchItemBinding,
    private val listener: StopwatchListener
) : RecyclerView.ViewHolder(binding.root) {

    private var timer: CountDownTimer? = null

    fun bind(stopwatch: Stopwatch) {
        binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
        binding.customView.setPeriod(stopwatch.startPeriod)
        if (stopwatch.isStarted) {
            startTimer(stopwatch)
        } else {
            stopTimer(stopwatch)
        }

        initButtonsListeners(stopwatch)
    }

    private fun initButtonsListeners(stopwatch: Stopwatch) {
        binding.startPauseButton.setOnClickListener {
            if (stopwatch.isStarted) {
                listener.stop(stopwatch.id, stopwatch.currentMs)
            } else {
                listener.start(stopwatch.id)
            }
        }

        binding.restartButton.setOnClickListener { listener.reset(stopwatch.id, stopwatch.startPeriod) }

        binding.deleteButton.setOnClickListener { listener.delete(stopwatch.id) }
    }

    private fun startTimer(stopwatch: Stopwatch) {
        val drawable = getDrawable(binding.root.context, R.drawable.ic_baseline_pause_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()
        timer = getCountDownTimer(stopwatch)
        timer?.start()

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(stopwatch: Stopwatch) {
        val drawable = getDrawable(binding.root.context, R.drawable.ic_baseline_play_arrow_24)
        binding.startPauseButton.setImageDrawable(drawable)

        timer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(stopwatch: Stopwatch): CountDownTimer {
        return object : CountDownTimer(stopwatch.startPeriod, UNIT_TEN_MS) {
            val interval = UNIT_TEN_MS

            override fun onTick(millisUntilFinished: Long) {
                stopwatch.currentMs -= interval
                binding.customView.setCurrent(stopwatch.startPeriod - stopwatch.currentMs)
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }

            override fun onFinish() {
                binding.stopwatchTimer.text = stopwatch.currentMs.displayTime()
            }
        }
    }
}