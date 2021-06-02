package com.dayaonweb.quoter.view.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dayaonweb.quoter.databinding.QuoteItemBinding
import com.dayaonweb.quoter.service.model.Quote
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar


class HomePostsViewHolder(itemView: QuoteItemBinding) : RecyclerView.ViewHolder(itemView.root) {

    private val binding = itemView

    fun bind(quote: Quote) {
        binding.tvContent.text = quote.content
        binding.tvAuthor.text = quote.author
        binding.btnCopy.setOnClickListener {
            copyToClipBoard(quote.content)
            Snackbar.make(binding.root,"Copied successfully",Snackbar.LENGTH_SHORT).show()
        }
        binding.root.setOnClickListener {
            Snackbar.make(binding.root,"Clicked!",Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun copyToClipBoard(content: String) {
        val clipboard = ContextCompat.getSystemService(binding.btnCopy.context, ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("",content))
    }
}