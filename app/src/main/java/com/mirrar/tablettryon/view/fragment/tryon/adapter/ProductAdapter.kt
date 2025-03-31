package com.mirrar.tablettryon.view.fragment.tryon.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.databinding.ProductCardBinding
import com.mirrar.tablettryon.products.model.product.Product

class ProductAdapter(private val clickListener: (Int, Product) -> Unit) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    private lateinit var ctx: Context

    private val list = mutableListOf<Product>()
    private var selectedIndex = -1

    private var onLoading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
        holder: ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {

//        if (recommendationModel != null && recommendationModel!!.recommendations.isNotEmpty()) {
//            holder.binding.recommendationTag.isVisible =
//                recommendationModel!!.recommendations.contains(list[position].objectID)
//        } else {
//            holder.binding.recommendationTag.isVisible = false
//        }

        holder.binding.progress.isVisible = position == list.size - 1 && onLoading

        holder.binding.recommendationTag.isVisible = list[position].recommended == true

        holder.binding.selectorHighlight.isVisible = selectedIndex == position
        holder.binding.nonSelectorHighlight.isVisible = selectedIndex != position

        val url = if (!list[position].asset2DUrl.isNullOrBlank()) {
            list[position].asset2DUrl
        } else if (!list[position].imageThumbnail.isNullOrBlank()) {
            list[position].imageThumbnail
        } else if (!list[position].imageSmall.isNullOrBlank()) {
            list[position].imageSmall
        } else {
            list[position].imageUrlBase ?: ""
        }

        Glide.with(ctx).load(url).into(holder.binding.thumb)
        holder.binding.root.setOnClickListener {
            clickListener(position, list[position])
            val oldPos = selectedIndex
            selectedIndex = position
            notifyItemChanged(oldPos)
            notifyItemChanged(selectedIndex)
        }

//        if (selectedIndex != -1 && selectedIndex < list.size) {
//            clickListener(selectedIndex, list[selectedIndex])
//        }
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Product>) {
        onLoading = false
        if (selectedIndex > list.size) {
            selectedIndex = -1
        } else if (list.isNotEmpty()) {
            selectedIndex = 0
            clickListener(selectedIndex, list[selectedIndex])
        }

        this.list.clear()
        this.list.addAll(list)

        notifyDataSetChanged()
    }

    fun addData(newProducts: List<Product>) {
        val startPos = list.size
        list.addAll(newProducts)
        notifyItemRangeInserted(startPos, newProducts.size)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        this.list.clear()
        notifyDataSetChanged()
    }

    fun onLoading(b: Boolean) {
        onLoading = b
        notifyItemChanged(list.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setPreSelected(p: Product) {
        if (list.contains(p)) {
            list.remove(p)
        }
        list.add(0, p)
        selectedIndex = 0
        notifyDataSetChanged()
    }

    fun scrollToPosition(i: Int) {
        val oldPos = selectedIndex
        selectedIndex = i
        notifyItemChanged(oldPos)
        notifyItemChanged(selectedIndex)
        clickListener(selectedIndex, list[selectedIndex])
    }
}