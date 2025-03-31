package com.mirrar.tablettryon.view.fragment.catalogue.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.FilterChipItemBinding

class FilterChipAdapter : RecyclerView.Adapter<FilterChipAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: FilterChipItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private var selectedPosition = -1

    private lateinit var ctx: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        this.ctx = parent.context
        return ViewHolder(
            FilterChipItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        holder.binding.filterTitle.text = "Quick Filter ${position + 1}"
        if (selectedPosition == position) {
            holder.binding.filterTitle.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.white
                )
            )
            holder.binding.parent.setCardBackgroundColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.primery_purple
                )
            )
        } else {
            holder.binding.filterTitle.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.black
                )
            )
            holder.binding.parent.setCardBackgroundColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.chip_gray
                )
            )
        }


        holder.binding.parent.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return 10
    }
}