package com.dayaonweb.quoter.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.QuoteItemBinding
import com.dayaonweb.quoter.service.model.Quote
import com.dayaonweb.quoter.view.ui.home.HomeQuoteDetailArgs

private const val TAG = "HomePostsAdapter"


class HomePostsAdapter: PagingDataAdapter<Quote, HomePostsViewHolder>(quoteComparator) {


    companion object {
        private val quoteComparator = object : DiffUtil.ItemCallback<Quote>() {
            override fun areItemsTheSame(oldItem: Quote, newItem: Quote): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Quote, newItem: Quote): Boolean =
                oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: HomePostsViewHolder, position: Int) {
        getItem(position)?.let { quote ->
            holder.bind(quote)
            holder.itemView.setOnClickListener {
               Log.d(TAG, "onBindViewHolder: Called")
//                onQuoteItemClicked
                val extras = FragmentNavigatorExtras(it to "quoteCardMain")
                val arguments = HomeQuoteDetailArgs(quote.authorSlug,quote.author)
                findNavController(it).navigate(R.id.action_home_to_homeQuoteDetail,arguments.toBundle(), null, extras)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePostsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = QuoteItemBinding.inflate(layoutInflater, parent, false)
        return HomePostsViewHolder(itemBinding)
    }
}