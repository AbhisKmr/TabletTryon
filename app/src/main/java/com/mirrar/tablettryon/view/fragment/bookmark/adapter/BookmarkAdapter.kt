package com.mirrar.tablettryon.view.fragment.bookmark.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.databinding.BookmarkItemBinding
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

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
        Glide.with(ctx).load(list[position].ImageLink).into(holder.binding.glassImage)
        holder.binding.productName.text = list[position].Brand
        holder.binding.productDetails.text = list[position].Description

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