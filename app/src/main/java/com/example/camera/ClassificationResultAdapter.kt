package com.example.camera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.camera.tflite.Classifier

class ClassificationResultAdapter : RecyclerView.Adapter<ResultViewHolder>() {

    var items: List<Classifier.Recognition> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_processing_result, parent, false))

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        items[position].let { item ->
            holder.txtId.text = item.id
            holder.txtObjectName.text = item.title
            holder.txtConfidence.text = item.confidence.toString()
        }
    }

    override fun getItemCount() = items.size

}

class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val txtId: TextView = view.findViewById(R.id.txt_id)
    val txtObjectName: TextView = view.findViewById(R.id.txt_object_name)
    val txtConfidence: TextView = view.findViewById(R.id.txt_confidence)
}