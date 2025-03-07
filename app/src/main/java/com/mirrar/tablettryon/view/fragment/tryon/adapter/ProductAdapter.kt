package com.mirrar.tablettryon.view.fragment.tryon.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.databinding.ProductCardBinding
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

class ProductAdapter(private val clickListener: (Product) -> Unit) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var ctx: Context

    private val list = mutableListOf<Product>()
    private var selectedIndex = -1

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

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ProductAdapter.ViewHolder, @SuppressLint("RecyclerView") position: Int) {

//        holder.binding.recommendationTag.isVisible = position % 2 == 0
        holder.binding.selectorHighlight.isVisible = selectedIndex == position

        Glide.with(ctx).load(list[position].ImageLink).into(holder.binding.thumb)

        holder.binding.root.setOnClickListener {
            clickListener(list[position])
            selectedIndex = position
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Product>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}