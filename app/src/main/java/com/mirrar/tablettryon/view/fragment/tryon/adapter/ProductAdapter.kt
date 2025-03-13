package com.mirrar.tablettryon.view.fragment.tryon.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.databinding.ProductCardBinding
import com.mirrar.tablettryon.utility.AppConstraint.SELECTED_INDEX
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

class ProductAdapter(private val clickListener: (Int, Product) -> Unit) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var ctx: Context

    private val list = mutableListOf<Product>()

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
    override fun onBindViewHolder(
        holder: ProductAdapter.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

        if (recommendationModel != null && recommendationModel!!.recommendations.isNotEmpty()) {
            holder.binding.recommendationTag.isVisible =
                recommendationModel!!.recommendations.contains(list[position].objectID)
        } else {
            holder.binding.recommendationTag.isVisible = false
        }

        holder.binding.selectorHighlight.isVisible = SELECTED_INDEX == position

        val url = if (!list[position].imageSmall.isNullOrBlank()) {
            list[position].imageSmall
        } else if (!list[position].imageThumbnail.isNullOrBlank()) {
            list[position].imageThumbnail
        } else {
            list[position].imageUrlBase ?: ""
        }

        Glide.with(ctx).load(url).into(holder.binding.thumb)
        holder.binding.root.setOnClickListener {
            clickListener(position, list[position])
            SELECTED_INDEX = position
            notifyDataSetChanged()
        }

        if (SELECTED_INDEX != -1 && SELECTED_INDEX < list.size) {
            clickListener(SELECTED_INDEX, list[SELECTED_INDEX])
        }
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Product>) {
        if (SELECTED_INDEX > list.size) {
            SELECTED_INDEX = -1
        }
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}