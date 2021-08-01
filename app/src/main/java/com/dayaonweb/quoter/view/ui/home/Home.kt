package com.dayaonweb.quoter.view.ui.home

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentHomeBinding
import com.dayaonweb.quoter.view.adapter.HomePostsAdapter
import com.dayaonweb.quoter.view.adapter.QuotesLoadStateAdapter

private const val TAG = "Home"

class Home : Fragment() {
    private var binding: FragmentHomeBinding? = null
    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val quotesAdapter = HomePostsAdapter()
        binding?.rvBrowseQuotes?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = quotesAdapter.withLoadStateHeaderAndFooter(
                header = QuotesLoadStateAdapter(quotesAdapter::retry),
                footer = QuotesLoadStateAdapter(quotesAdapter::retry)
            )
        }

        quotesAdapter.addLoadStateListener { loadState ->
            with(binding) {
                binding?.loader?.isVisible = loadState.source.refresh is LoadState.Loading
                this?.rvBrowseQuotes?.isVisible = loadState.source.refresh is LoadState.NotLoading
                this?.llErrorState?.isVisible = loadState.source.refresh is LoadState.Error
            }
        }
        binding?.btnRetry?.setOnClickListener {
            quotesAdapter.retry()
        }
        viewModel.quotes.observe(viewLifecycleOwner) {
            quotesAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}