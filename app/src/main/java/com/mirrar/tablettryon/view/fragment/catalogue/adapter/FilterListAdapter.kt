package com.mirrar.tablettryon.view.fragment.catalogue.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.mirrar.tablettryon.databinding.FilterSelectItemBinding
import com.mirrar.tablettryon.tools.filter.FilterDataModel

class FilterListAdapter(private val lst: List<FilterDataModel>) :
    RecyclerView.Adapter<FilterListAdapter.ViewHolder>() {

    class ViewHolder(val binding: FilterSelectItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            FilterSelectItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.title.text = lst[position].value
    }

    override fun getItemCount(): Int = lst.size
}