package com.mirrar.tablettryon.view.fragment.tryon.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettryon.databinding.ProductCardBinding

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var ctx: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductAdapter.ViewHolder {
        this.ctx = parent.context
        return ViewHolder(
            ProductCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductAdapter.ViewHolder, position: Int) {

        holder.binding.recommendationTag.isVisible = position % 2 == 0

    }

    override fun getItemCount(): Int {
        return 20
    }
}