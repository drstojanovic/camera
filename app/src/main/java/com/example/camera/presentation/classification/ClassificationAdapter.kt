package com.example.camera.presentation.classification

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.camera.databinding.ItemClassificationBinding

class ClassificationAdapter : RecyclerView.Adapter<ClassificationAdapter.ClassificationViewHolder>() {

    private var items = arrayListOf<ClassificationResultView>()

    override fun getItemCount() = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassificationViewHolder =
        ClassificationViewHolder(ItemClassificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ClassificationViewHolder, position: Int) =
        holder.bind(items[position])

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(items: List<ClassificationResultView>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    class ClassificationViewHolder(val binding: ItemClassificationBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ClassificationResultView) {
            binding.data = item
        }
    }
}
