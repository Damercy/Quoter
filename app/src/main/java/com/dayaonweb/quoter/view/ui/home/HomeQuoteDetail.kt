package com.dayaonweb.quoter.view.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.dayaonweb.quoter.enums.Status
import com.dayaonweb.quoter.extensions.isVisible
import com.dayaonweb.quoter.extensions.loadImageUri
import com.dayaonweb.quoter.extensions.snack

private const val TAG = "HomeQuoteDetail"

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
        super.onCreate(savedInstanceState)
        viewModel.fetchAuthorImage(args.authorName.trim().replace(" ", "_", true))
        viewModel.fetchAuthorDetailsBySlug(args.authorSlug)
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.statusBarColor = Color.TRANSPARENT
        (activity as AppCompatActivity).supportActionBar?.hide()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.navToolbar?.apply {
            setNavigationOnClickListener {
                activity?.onBackPressed()
            }
            title = args.authorName
        }
        viewModel.author.observe({ lifecycle }) { author ->
            binding?.loader?.isVisible(false)
            binding?.tvAuthorBio?.text = author.bio
            binding?.tvAuthorDescription?.text = author.description
        }
        viewModel.authorImage.observe({ lifecycle }) { authorImageUrl ->
            binding?.ivAuthorImage?.loadImageUri(authorImageUrl)
        }
        viewModel.status.observe({ lifecycle }) { status ->
            Log.d(TAG, "onViewCreated: Called status value=$status")
            if (status == Status.READ_FAIL) {
                requireView().snack("Failed to load data. Try later!")
                binding?.loader?.isVisible(false)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        activity?.window?.statusBarColor =
            resources.getColor(R.color.design_default_color_primary_variant, null)
    }


}