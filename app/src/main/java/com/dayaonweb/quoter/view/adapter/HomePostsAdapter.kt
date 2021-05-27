package com.dayaonweb.quoter.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dayaonweb.quoter.databinding.QuoteItemBinding
import com.dayaonweb.quoter.service.model.Quote


class HomePostsAdapter : PagingDataAdapter<Quote, HomePostsViewHolder>(quoteComparator) {


    companion object {
        private val quoteComparator = object : DiffUtil.ItemCallback<Quote>() {
            override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: HomePostsViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePostsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = QuoteItemBinding.inflate(layoutInflater, parent, false)
        return HomePostsViewHolder(itemBinding)
    }
}