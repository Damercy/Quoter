package com.dayaonweb.quoter.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dayaonweb.quoter.databinding.LoadStateAdapterBinding
import com.dayaonweb.quoter.extensions.isVisible

class QuotesLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<QuotesLoadStateAdapter.QuotesLoadStateViewHolder>() {

    override fun onBindViewHolder(holder: QuotesLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): QuotesLoadStateViewHolder {
        val binding = LoadStateAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuotesLoadStateViewHolder(binding)
    }


    inner class QuotesLoadStateViewHolder(val binding: LoadStateAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnRetry.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState) {
            with(binding) {
                loader.isVisible(loadState is LoadState.Loading)
                llErrorState.isVisible(loadState is LoadState.Error)
            }
        }
    }
}