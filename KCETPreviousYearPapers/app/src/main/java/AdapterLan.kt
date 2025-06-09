package com.kea.pyp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kea.pyp.databinding.ItemViewLanBinding

class AdapterLan (private val lanitems: List<YearItem>,
                  private val itemClickListener: (YearItem)-> Unit) : RecyclerView.Adapter<AdapterLan.LanViewHolder>() {

    inner class LanViewHolder(private val binding: ItemViewLanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: YearItem) {
            binding.descriptionLan.text = item.descLan
            binding.root.setOnClickListener{
                itemClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemViewLanBinding.inflate(inflater, parent, false)
        return LanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanViewHolder, position: Int) {
        holder.bind(lanitems[position])
    }

    override fun getItemCount(): Int = lanitems.size
}