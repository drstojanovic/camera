package com.example.camera.utils.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.camera.R
import com.example.camera.utils.ActionListener

class SeekBarCompositeView(context: Context, attributeSet: AttributeSet?) : ConstraintLayout(context, attributeSet) {

    private val slider by lazy { findViewById<SeekBar>(R.id.seek_bar) }
    private val txtLabel by lazy { findViewById<TextView>(R.id.txt_label) }
    private val txtValue by lazy { findViewById<TextView>(R.id.txt_current_value) }
    private val txtMin by lazy { findViewById<TextView>(R.id.txt_range_label_min) }
    private val txtMax by lazy { findViewById<TextView>(R.id.txt_range_label_max) }
    private var stepSize: Int = 1
    private var min: Int = 0
    private var max: Int = 100

    init {
        View.inflate(context, R.layout.view_seek_bar_composite, this)
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.SeekBarCompositeView, 0, 0).apply {
            try {
                txtLabel.text = getString(R.styleable.SeekBarCompositeView_text)
                stepSize = getInt(R.styleable.SeekBarCompositeView_stepSize, 1)
                setMin(getInt(R.styleable.SeekBarCompositeView_minValue, 1))
                setMax(getInt(R.styleable.SeekBarCompositeView_maxValue, 1))
            } finally {
                recycle()
            }
        }
    }

    fun setProgressListener(listener: ActionListener) {
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                (min + progress * stepSize).let { realProgress ->
                    listener.onAction(realProgress)
                    txtValue.text = realProgress.toString()
                }
            }
        })
    }

    fun setProgress(progress: Int) {
        slider.progress = (progress - min) / stepSize
        txtValue.text = progress.toString()
    }

    private fun setMin(min: Int) {
        this.min = min
        txtMin.text = min.toString()
    }

    private fun setMax(max: Int) {
        this.max = max
        slider.max = (max - min) / stepSize
        txtMax.text = max.toString()
    }
}
