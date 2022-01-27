package com.example.camera.presentation.main

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.camera.R

class DetectionAdapter(resources: Resources) : RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {

    private var items = ArrayList<String>()
    private val colors = resources.getIntArray(R.array.bbox_colors)

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder =
        DetectionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_detection, parent, false))

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        holder.textView.apply {
            text = items[position]
            setTextColor(colors[position % colors.size])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<String>) {
        this.items = ArrayList(items)
        notifyDataSetChanged()
    }

    class DetectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.txt_detection)
    }
}
