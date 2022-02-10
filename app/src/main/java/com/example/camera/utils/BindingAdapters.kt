package com.example.camera.utils

import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.camera.presentation.classification.ClassificationAdapter
import com.example.camera.presentation.classification.ClassificationResultView
import com.example.camera.presentation.detection.DetectionAdapter

@BindingAdapter("selected")
fun View.bindSelectionState(isSelected: Boolean) {
    this.isSelected = isSelected
}

@BindingAdapter("visibility")
fun View.bindVisibility(isVisible: Boolean) {
    this.isVisible = isVisible
}

@BindingAdapter("android:text")
fun TextView.bindNullableStringResource(resource: Int?) {
    resource?.let { setText(it) } ?: run { text = "" }
}

@BindingAdapter("onTextChange")
fun EditText.bindTextChangeListener(textChangeListener: StringActionListener?) {
    doOnTextChanged { text, _, _, _ -> textChangeListener?.onAction(text?.toString() ?: "") }
}

@BindingAdapter(value = ["items", "selectedIndex", "onItemSelected"], requireAll = false)
fun Spinner.bindSpinnerItems(items: List<String>, selectedIndex: Int?, onItemSelected: ActionListener?) {
    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onItemSelected?.onAction(position)
        }
    }
    selectedIndex?.let { setSelection(it) }
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

@BindingAdapter("items")
fun RecyclerView.bindItems(items: List<String>) {
    (adapter as DetectionAdapter).setItems(items)
}

@BindingAdapter("classifications")
fun RecyclerView.bindClassificationItems(items: List<ClassificationResultView>?) {
    items?.let { (adapter as ClassificationAdapter).setItems(items) }
}

interface ActionListener {
    fun onAction(parameter: Int)
}

interface StringActionListener {
    fun onAction(parameter: String)
}
