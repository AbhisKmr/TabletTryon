package com.mirrar.tablettryon.view.fragment.selfie.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.databinding.SelfieItemBinding
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.utility.AppConstraint.AR_BITMAP
import com.mirrar.tablettryon.utility.HelperFunctions.isValidUrl

class SelfieAdapter(val list: List<Product>) : RecyclerView.Adapter<SelfieAdapter.ViewHolder>() {

    private lateinit var ctx: Context

    class ViewHolder(val binding: SelfieItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        this.ctx = parent.context
        return ViewHolder(
            SelfieItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val p = list[position]

        holder.binding.brand.text = p.brand
        holder.binding.productCode.text = p.localItemCode
        holder.binding.productPrice.text =
            "${p.currency} ${p.priceDutyFree}"

        if (isValidUrl(p.triedOnUrl)) {
            Glide.with(ctx).load(p.triedOnUrl).into(holder.binding.modelPreview)
        }
        else {
            holder.binding.modelPreview.setImageBitmap(AR_BITMAP)
        }
    }

    override fun getItemCount(): Int = list.size
}