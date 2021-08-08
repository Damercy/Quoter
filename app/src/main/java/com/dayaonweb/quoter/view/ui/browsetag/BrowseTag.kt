package com.dayaonweb.quoter.view.ui.browsetag

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.FragmentBrowseTagBinding
import com.dayaonweb.quoter.service.model.Quote
import com.google.android.material.snackbar.Snackbar

class BrowseTag : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var bi: FragmentBrowseTagBinding? = null
    private val viewModel: BrowseTagViewModel by viewModels()
    private var quoteToAuthor = mutableMapOf<Quote, String>()
    private var totalPages = -1
    private var pageToFetch = 1
    private var currentPageCount = 0
    private var currentQuoteNumber = 1
    private val args: BrowseTagArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bi = DataBindingUtil.inflate(inflater, R.layout.fragment_browse_tag, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
        attachObservers()
        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
    }

    private fun attachObservers() {
        viewModel.quotes.observe({ lifecycle }) {
            currentPageCount += it.count
            totalPages = it.totalPages
            for (result in it.results) {
                quoteToAuthor[result] = result.author
            }
            initNumberPicker()
        }
    }

    private fun attachListeners() {
        bi?.apply {
            shareImageView.setOnClickListener {

            }
            optionsImageView.setOnClickListener {
                showPopup(it)
            }
            backImageView.setOnClickListener {
                requireActivity().onBackPressed()
            }
            quoteScroller.setOnValueChangedListener { _, _, newVal ->
                currentQuoteNumber = newVal+1
                val currentQuote = quoteToAuthor.keys.toTypedArray()[newVal]
                quoteTextView.text = currentQuote.content
                authorTextView.text = quoteToAuthor.values.toTypedArray()[newVal]
                serialTextView.text = String.format("%s", "$currentQuoteNumber/$currentPageCount")
                quoteTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if (currentQuote.length > 150) 24f else 32f
                )
                if (currentPageCount - (newVal + 1) <= 4) {
                    pageToFetch++
                    if (pageToFetch <= totalPages && !viewModel.isFetchingQuotes)
                        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
                }
            }

        }

    }

    private fun initNumberPicker() {
        bi?.quoteScroller?.apply {
            isVisible = true
            typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
            setSelectedTypeface(ResourcesCompat.getFont(requireContext(), R.font.main_bold))
            minValue = 0
            maxValue = currentPageCount - 1
            displayedValues = Array(quoteToAuthor.size) { "" }
        }
        bi?.quoteTextView?.text = quoteToAuthor.keys.toTypedArray()[0].content
        bi?.authorTextView?.text = quoteToAuthor.values.toTypedArray()[0]
        bi?.serialTextView?.text = String.format("%s", "$currentQuoteNumber/$currentPageCount")
        bi?.shareImageView?.isVisible = true
        bi?.backImageView?.isVisible = true
        bi?.quoteImageView?.isVisible = true
        bi?.optionsImageView?.isVisible = true
    }

    private fun showPopup(anchorView: View) {
        val wrapper = ContextThemeWrapper(requireContext(), R.style.BasePopupMenu)
        PopupMenu(wrapper, anchorView).apply {
            setOnMenuItemClickListener(this@BrowseTag)
            gravity = Gravity.END
            inflate(R.menu.options_menu)
            show()
        }

    }

    override fun onDestroy() {
        bi = null
        super.onDestroy()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.copy_quote -> {
                bi?.quoteTextView?.text?.let { quote ->
                    bi?.authorTextView?.text?.let { author ->
                        copyText(String.format("%s\n\n~ %s", quote, author))
                    }
                }
                true
            }
            else -> false
        }
    }

    private fun copyText(text: String) {
        val clipboard = requireActivity().getSystemService(ClipboardManager::class.java)
        val clip = ClipData.newPlainText("quote", text)
        clipboard.setPrimaryClip(clip)
        showSnack()
    }

    private fun showSnack() {
        val snack = Snackbar.make(requireView(), "Copied", Snackbar.LENGTH_SHORT)
        val snackView = snack.view
        snackView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        val snackText =
            snackView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackText.typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
        snack.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        snack.show()
    }

}