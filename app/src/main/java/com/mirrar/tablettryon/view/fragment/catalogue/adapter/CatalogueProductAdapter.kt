package com.mirrar.tablettryon.view.fragment.catalogue.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.CatalogueProductItemBinding
import com.mirrar.tablettryon.utility.AppConstraint.recommendationModel
import com.mirrar.tablettryon.utility.Bookmarks
import com.mirrar.tablettryon.view.fragment.tryon.dataModel.Product

class CatalogueProductAdapter(private val clickListener: (Int, Product) -> Unit) :
    RecyclerView.Adapter<CatalogueProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CatalogueProductItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    private lateinit var ctx: Context
    private val list = mutableListOf<Product>()
    private val bookmarks = Bookmarks.getBookmarks()

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

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: CatalogueProductAdapter.ViewHolder, position: Int) {

        val p = list[position]
        /*holder.binding.tag.setImageDrawable(
            if (position < 4) {
                ContextCompat.getDrawable(ctx, R.drawable.recommended_orange)
            } else if (position < 5) {
                ContextCompat.getDrawable(ctx, R.drawable.new_tag)
            } else {
                null
            }
        )*/

        if (recommendationModel != null && recommendationModel!!.recommendations.isNotEmpty()) {
            holder.binding.tag.setImageDrawable(
                ContextCompat.getDrawable(
                    ctx,
                    R.drawable.recommended_orange
                )
            )
        }

        updateHeartIcon(holder.binding.wishlist, p.isBookmarked)

        holder.binding.brand.text = p.brand
        holder.binding.productCode.text = p.localItemCode
        holder.binding.productPrice.text =
            "${p.currency} ${p.priceDutyFree}"


        val url = if (!p.imageSmall.isNullOrBlank()) {
            p.imageSmall
        } else if (!p.imageThumbnail.isNullOrBlank()) {
            p.imageThumbnail
        } else {
            p.imageUrlBase ?: ""
        }

        Glide.with(ctx).load(url).into(holder.binding.thumb)

        holder.binding.root.setOnClickListener {
            clickListener(position, p)
        }

        holder.binding.wishlist.setOnClickListener {
            Bookmarks.addToBookmark(p)
            p.isBookmarked = !p.isBookmarked
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(list: List<Product>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    private fun updateHeartIcon(iv: ImageView, b: Boolean) {

        val drawable = if (b) {
            ContextCompat.getDrawable(ctx, R.drawable.ic_heart_red)
        } else {
            ContextCompat.getDrawable(ctx, R.drawable.ic_heart_gray)
        }

        iv.setImageDrawable(drawable)
    }
}