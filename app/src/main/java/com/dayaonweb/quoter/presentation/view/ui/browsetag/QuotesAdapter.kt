package com.dayaonweb.quoter.presentation.view.ui.browsetag

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dayaonweb.quoter.databinding.ItemQuoteBinding
import com.dayaonweb.quoter.domain.models.UiQuote

class QuotesAdapter :
    ListAdapter<UiQuote, QuotesAdapter.QuotesViewHolder>(object : DiffUtil.ItemCallback<UiQuote>() {
        override fun areItemsTheSame(
            oldItem: UiQuote,
            newItem: UiQuote
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: UiQuote,
            newItem: UiQuote
        ) = oldItem == newItem
    }) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QuotesViewHolder {
        val binding = ItemQuoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuotesViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: QuotesViewHolder,
        position: Int
    ) {
        holder.bind(quote = getItem(position))
    }

    class QuotesViewHolder(val binding: ItemQuoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(quote: UiQuote) {
            binding.quoteTextView.text = quote.quote
            binding.authorTextView.text = quote.author
        }
    }
}