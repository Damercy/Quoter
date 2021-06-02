package com.dayaonweb.quoter.view.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentQuoteDetailBinding

class HomeQuoteDetail : Fragment() {

    private var binding: FragmentQuoteDetailBinding? = null
    private val viewModel by viewModels<HomeViewModel>()
    private val args: HomeQuoteDetailArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_quote_detail, container, false)
        return binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (activity as AppCompatActivity).supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        viewModel.fetchAuthorDetailsBySlug(args.authorSlug)
//        val animation =
//            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.fade)
//        sharedElementEnterTransition = animation
//        sharedElementReturnTransition = animation
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.navToolbar?.apply {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
            title = args.authorName
        }
        viewModel.author.observe({lifecycle}){ author ->
            binding?.tvAuthorBio?.text = author.bio
            binding?.tvAuthorDescription?.text = author.description
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


}