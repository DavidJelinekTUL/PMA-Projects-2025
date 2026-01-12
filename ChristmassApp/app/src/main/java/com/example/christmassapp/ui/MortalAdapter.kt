package com.example.christmassapp.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.christmassapp.databinding.ItemSoulBinding
import com.example.christmassapp.model.MortalSoul

class MortalAdapter(
    private val souls: List<MortalSoul>,
    private val onSoulClick: (Int) -> Unit
) : RecyclerView.Adapter<MortalAdapter.SoulViewHolder>() {

    inner class SoulViewHolder(val binding: ItemSoulBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoulViewHolder {
        val binding = ItemSoulBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SoulViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SoulViewHolder, position: Int) {
        val soul = souls[position]
        holder.binding.tvName.text = soul.name
        holder.binding.tvScore.text = "Hřích: ${soul.sinScore}%"

        if (soul.sinScore > 50) holder.binding.tvScore.setTextColor(Color.RED)
        else holder.binding.tvScore.setTextColor(Color.GREEN)

        holder.itemView.setOnClickListener { onSoulClick(soul.id) }
    }

    override fun getItemCount() = souls.size
}