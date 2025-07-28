package com.kea.pyp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kea.pyp.databinding.ItemViewLanBinding

class UpdatesAdapter(val itemClickListener: (InfoItem) -> Unit) :
    ListAdapter<InfoItem, UpdatesAdapter.ViewHolder>(InfoItemDiffCallback()) {

    inner class ViewHolder(private val binding: ItemViewLanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InfoItem) {
            binding.descriptionLan.text = item.description
            binding.root.setOnClickListener {
                itemClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemViewLanBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class InfoItemDiffCallback : DiffUtil.ItemCallback<InfoItem>() {
    override fun areItemsTheSame(oldItem: InfoItem, newItem: InfoItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: InfoItem, newItem: InfoItem) = oldItem == newItem
}