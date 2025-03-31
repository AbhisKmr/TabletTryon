package com.mirrar.tablettryon.view.fragment.bookmark.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.databinding.BookmarkItemBinding
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.utility.Bookmarks

class BookmarkAdapter : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    private val list = mutableListOf<Product>()
    private lateinit var ctx: Context

    inner class ViewHolder(val binding: BookmarkItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.ctx = parent.context
        return ViewHolder(
            BookmarkItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val url = if (!list[position].imageSmall.isNullOrBlank()) {
            list[position].imageSmall
        } else if (!list[position].imageThumbnail.isNullOrBlank()) {
            list[position].imageThumbnail
        } else {
            list[position].imageUrlBase ?: ""
        }

        Glide.with(ctx).load(url).into(holder.binding.glassImage)
        holder.binding.productName.text = list[position].brand
        holder.binding.productDetails.text = list[position].localItemCode
        holder.binding.price.text = "${list[position].currency} ${list[position].priceDutyFree}"

        holder.binding.delete.setOnClickListener {
            Bookmarks.removeBookmark(list[position])
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Product>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}