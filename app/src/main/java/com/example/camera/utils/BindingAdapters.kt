package com.example.camera.utils

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter

@BindingAdapter(value = ["items", "onItemSelected"], requireAll = false)
fun Spinner.bindSpinnerItems(items: List<String>, onItemSelected: ActionListener?) {
    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
   onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
       override fun onNothingSelected(parent: AdapterView<*>?)  = Unit
       override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
           onItemSelected?.onAction(position)
       }
   }
}

interface ActionListener {
    fun onAction(parameter: Int)
}
