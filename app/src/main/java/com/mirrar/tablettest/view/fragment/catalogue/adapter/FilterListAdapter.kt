package com.mirrar.tablettest.view.fragment.catalogue.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettest.databinding.FilterSelectItemBinding
import com.mirrar.tablettest.tools.filter.FilterDataModel

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
        holder.binding.title.isChecked = lst[position].isSelected
        holder.binding.root.setOnClickListener {
            lst[position].isSelected = !lst[position].isSelected
            notifyItemChanged(position)
            onSelect()
        }
    }

    override fun getItemCount(): Int = lst.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateSelection(brandList: MutableList<String>) {
        this.lst.forEach {
            it.isSelected = brandList.contains(it.value)
        }
        notifyDataSetChanged()
    }
}