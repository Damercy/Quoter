package com.dayaonweb.quoter.view.ui.browsetag

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.TypedValue
import android.view.*
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.analytics.Analytics
import com.dayaonweb.quoter.databinding.FragmentBrowseTagBinding
import com.dayaonweb.quoter.service.model.Quote
import com.dayaonweb.quoter.tts.Quoter
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.util.*

class BrowseTag : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var bi: FragmentBrowseTagBinding? = null
    private val viewModel: BrowseTagViewModel by viewModels()
    private var quoteToAuthor = mutableMapOf<Quote, String>()
    private var totalPages = -1
    private var pageToFetch = 1
    private var currentPageCount = 0
    private var currentQuoteNumber = 1
    private lateinit var quoterSpeaker: Quoter
    private val args: BrowseTagArgs by navArgs()
    private lateinit var speakerAnimation: AnimationDrawable

    /**************ANALYTICS********************/
    private var currentQuoteId: String = ""
    private lateinit var currentQuoteTag: List<String>

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
        initQuoter()
        attachListeners()
        attachObservers()
        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
    }

    private fun initQuoter() {
        quoterSpeaker = Quoter(context = requireContext()) { initStatus ->
            if (initStatus == TextToSpeech.SUCCESS)
                quoterSpeaker.init(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        requireActivity().runOnUiThread {
                            speakerAnimation.start()
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        requireActivity().runOnUiThread {
                            speakerAnimation.stop()
                            speakerAnimation.selectDrawable(0)
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        requireActivity().runOnUiThread {
                            speakerAnimation.stop()
                            speakerAnimation.selectDrawable(0)
                            showSnack("Unable to synthesize text")
                        }
                    }
                })
        }
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
        viewModel.ssFile.observe({ lifecycle }) {
            shareScreenshot(
                FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    it
                )
            )
        }
    }

    private fun shareScreenshot(fileUri: Uri) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Sent via Quoter. Download now: https://play.google.com/store/apps/details?id=com.dayaonweb.quoter"
            )
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "image/jpg"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share this quote with...")
        startActivity(shareIntent)
    }

    private fun attachListeners() {
        bi?.apply {
            shareImageView.setOnClickListener {
                Analytics.trackQuoteShare(
                    bi?.quoteTextView?.text.toString(),
                    currentQuoteId,
                    currentQuoteTag
                )
                bi?.screenshotView?.let { containerView ->
                    viewModel.takeScreenShot(
                        containerView,
                        File(requireContext().externalCacheDir, "quoter_${UUID.randomUUID()}.jpg")
                    )
                }
            }
            optionsImageView.setOnClickListener {
                showPopup(it)
            }
            backImageView.setOnClickListener {
                requireActivity().onBackPressed()
            }
            quoteScroller.setOnValueChangedListener { _, _, newVal ->
                currentQuoteNumber = newVal + 1
                val currentQuote = quoteToAuthor.keys.toTypedArray()[newVal]
                quoteTextView.text = currentQuote.content
                ssQuoteTextView.text = currentQuote.content
                authorTextView.text = quoteToAuthor.values.toTypedArray()[newVal]
                ssAuthorTextView.text = quoteToAuthor.values.toTypedArray()[newVal]
                serialTextView.text = String.format("%s", "$currentQuoteNumber/$currentPageCount")
                fadeInViews()
                quoteTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if (currentQuote.length > 150) 24f else 32f
                )
                ssQuoteTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if (currentQuote.length > 150) 18f else 28f
                )
                if (currentPageCount - (newVal + 1) <= 4) {
                    pageToFetch++
                    if (pageToFetch <= totalPages && !viewModel.isFetchingQuotes)
                        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
                }

                /**************ANALYTICS********************/
                currentQuoteId = currentQuote.id
                currentQuoteTag = currentQuote.tags
            }

            speakImageView.apply {
                setBackgroundResource(R.drawable.speaking_anim)
                speakerAnimation = background as AnimationDrawable
                setOnClickListener {
                    quoterSpeaker.speakText(bi?.quoteTextView?.text.toString(), currentQuoteId)
                }
            }
        }
    }

    private fun fadeInViews() {
        bi?.apply {
            authorTextView.alpha = 0.0f
            quoteTextView.alpha = 0.0f
            quoteImageView.alpha = 0.0f
            authorTextView.animate().alpha(1.0f).setDuration(200).start()
            quoteTextView.animate().alpha(1.0f).setDuration(200).start()
            quoteImageView.animate().alpha(1.0f).setDuration(200).start()
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
        bi?.ssQuoteTextView?.text = quoteToAuthor.keys.toTypedArray()[0].content
        bi?.authorTextView?.text = quoteToAuthor.values.toTypedArray()[0]
        bi?.ssAuthorTextView?.text = quoteToAuthor.values.toTypedArray()[0]
        bi?.serialTextView?.text = String.format("%s", "$currentQuoteNumber/$currentPageCount")
        bi?.shareImageView?.isVisible = true
        bi?.backImageView?.isVisible = true
        bi?.quoteImageView?.isVisible = true
        bi?.optionsImageView?.isVisible = true
        bi?.speakImageView?.isVisible = true
        bi?.loader?.isVisible = false

        /**************ANALYTICS********************/
        currentQuoteId = quoteToAuthor.keys.toTypedArray()[0].id
        currentQuoteTag = quoteToAuthor.keys.toTypedArray()[0].tags
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
                        Analytics.trackQuoteCopy(quote.toString(), currentQuoteId, currentQuoteTag)
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
        showSnack("Copied")
    }

    private fun showSnack(text: String) {
        val snack = Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT)
        val snackView = snack.view
        snackView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        val snackText =
            snackView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        snackText.typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
        snack.animationMode = Snackbar.ANIMATION_MODE_SLIDE
        snack.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        quoterSpeaker.deInit()
    }

    companion object {
        private const val TAG = "BrowseTag"
    }

}