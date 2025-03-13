package com.mirrar.tablettryon.view.fragment.catalogue.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.mirrar.tablettryon.databinding.FilterSelectItemBinding
import com.mirrar.tablettryon.tools.filter.FilterDataModel

class FilterListAdapter(private val lst: List<FilterDataModel>, val onSelect : () -> Unit) :
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
        holder.binding.root.setOnClickListener {
            lst[position].isSelected = !lst[position].isSelected
            onSelect()
        }
    }

    override fun getItemCount(): Int = lst.size
}