package com.example.camera.utils

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import androidx.databinding.BindingAdapter

@BindingAdapter(value = ["items", "selectedIndex", "onItemSelected"], requireAll = false)
fun Spinner.bindSpinnerItems(items: List<String>, selectedIndex: Int, onItemSelected: ActionListener?) {
    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected?.onAction(position)
        }
    }
    setSelection(selectedIndex)
}

@BindingAdapter(value = ["min", "max", "step", "progress", "onProgressChange"], requireAll = false)
fun SeekBar.bindSlideListener(
    min: Int,
    max: Int,
    step: Int?,
    progress: Int?,
    onProgressChangeListener: ActionListener
) {
    val nnStep = step ?: 1
    val realProgress = progress?.let { (it - min) / nnStep } ?: 0
    val realMax = (max - min) / nnStep
    val realMin = min / nnStep

    setMax(realMax)
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChangeListener.onAction((progress + realMin) * nnStep)
        }
    })
    setProgress(realProgress)
}

interface ActionListener {
    fun onAction(parameter: Int)
}
