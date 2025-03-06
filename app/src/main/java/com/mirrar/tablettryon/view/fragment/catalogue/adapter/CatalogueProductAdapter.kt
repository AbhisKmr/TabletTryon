package com.mirrar.tablettryon.view.fragment.catalogue.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.CatalogueProductItemBinding

class CatalogueProductAdapter : RecyclerView.Adapter<CatalogueProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CatalogueProductItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private lateinit var ctx: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CatalogueProductAdapter.ViewHolder {
        this.ctx = parent.context
        return ViewHolder(
            CatalogueProductItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CatalogueProductAdapter.ViewHolder, position: Int) {

        holder.binding.tag.setImageDrawable(
            if (position < 3) {
                ContextCompat.getDrawable(ctx, R.drawable.recommended_orange)
            } else if (position < 5) {
                ContextCompat.getDrawable(ctx, R.drawable.new_tag)
            } else {
                null
            }
        )

    }

    override fun getItemCount(): Int {
        return 20
    }
}