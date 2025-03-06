package com.mirrar.tablettryon.view.fragment.bookmark.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettryon.databinding.BookmarkItemBinding

class BookmarkAdapter : RecyclerView.Adapter<BookmarkAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: BookmarkItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            BookmarkItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return 15
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }
}