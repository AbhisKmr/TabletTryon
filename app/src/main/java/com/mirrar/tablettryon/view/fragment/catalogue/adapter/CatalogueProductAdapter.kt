package com.mirrar.tablettryon.view.fragment.catalogue.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mirrar.tablettryon.R
import com.mirrar.tablettryon.databinding.CatalogueProductItemBinding
import com.mirrar.tablettryon.products.model.product.Product
import com.mirrar.tablettryon.utility.Bookmarks

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

//        if (recommendationModel != null && recommendationModel!!.recommendations.isNotEmpty()) {
//            if (recommendationModel!!.recommendations.contains(list[position].objectID)) {
//                holder.binding.tag.setImageDrawable(
//                    ContextCompat.getDrawable(
//                        ctx,
//                        R.drawable.recommended_orange
//                    )
//                )
//            } else {
//                holder.binding.tag.setImageDrawable(null)
//            }
//        } else {
//            holder.binding.tag.setImageDrawable(null)
//        }
        holder.binding.tag.visibility =
            if (list[position].recommended == true) View.VISIBLE else View.INVISIBLE

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
//            Bookmarks.addToBookmark(p)
            p.isBookmarked = !p.isBookmarked
            notifyDataSetChanged()
        }
    }

    fun addData(newProducts: List<Product>) {
        val startPos = list.size
        list.addAll(newProducts)
        notifyItemRangeInserted(startPos, newProducts.size)
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

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        this.list.clear()
        notifyDataSetChanged()
    }
}