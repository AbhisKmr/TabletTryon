package com.mirrar.tablettest.tools.faceDetector.mlkit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettest.databinding.DetailsCardImageItemBinding

class CardSLideAdapter(private val items: Array<String>) :
    RecyclerView.Adapter<CardSLideAdapter.CardAdapter>() {

    class CardAdapter(val binding: DetailsCardImageItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardAdapter {
        return CardAdapter(
            DetailsCardImageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CardAdapter, position: Int) {
        Glide.with(holder.binding.thumb.context).load(items[position]).into(holder.binding.thumb)
    }
}