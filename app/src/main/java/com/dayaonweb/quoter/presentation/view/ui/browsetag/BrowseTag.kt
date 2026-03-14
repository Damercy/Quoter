package com.dayaonweb.quoter.presentation.view.ui.browsetag

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.domain.analytics.Analytics
import com.dayaonweb.quoter.databinding.FragmentBrowseTagBinding
import com.dayaonweb.quoter.domain.extensions.showSnack
import com.dayaonweb.quoter.domain.models.UiQuote
import com.dayaonweb.quoter.domain.tts.QuoteSpeaker
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BrowseTag : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var bi: FragmentBrowseTagBinding? = null
    private val viewModel: BrowseTagViewModel by viewModels()
    private var authorToQuote = mutableMapOf<String,UiQuote>()
    private var totalPages = -1
    private var pageToFetch = 1
    private var currentPageCount = 0
    private var currentQuoteNumber = 1
    @Inject
    lateinit var quoteSpeaker: Lazy<QuoteSpeaker>
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
        bi = FragmentBrowseTagBinding.inflate(inflater, container, false)
        return bi?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        attachListeners()
        attachObservers()
        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
    }

    private fun initQuoter(ttsLanguage: Locale) {
        bi?.ttsLoader?.isVisible = false
        bi?.speakImageView?.isVisible = true
        quoteSpeaker.get().setEngineLocale(ttsLanguage)
        quoteSpeaker.get().setSpeechRate(viewModel.preferences.value?.speechRate ?: 1.0f)
        val engineLocale = quoteSpeaker.get().getEngineLocale() ?: ttsLanguage
        viewModel.updateTtsLanguage(engineLocale)
    }

    private fun attachObservers() {
        viewModel.preferences.observe(viewLifecycleOwner) {
            initQuoter(it.ttsLanguage)
        }
        viewModel.quotes.observe(viewLifecycleOwner) {
            currentPageCount += it.size
            totalPages = it.size
            for (quote in it) {
                authorToQuote[quote.author] = quote
            }
            initNumberPicker()
        }
        viewModel.ssFile.observe(viewLifecycleOwner) {
            shareScreenshot(
                FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    it
                )
            )
        }
        quoteSpeaker.get().isSpeaking().asLiveData().observe(viewLifecycleOwner){ isSpeaking ->
            if(isSpeaking){
                speakerAnimation.start()
            }else{
                speakerAnimation.stop()
                speakerAnimation.selectDrawable(0)
            }
        }
    }

    private fun shareScreenshot(fileUri: Uri) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Checkout this cool quote.\nDownload app: https://play.google.com/store/apps/details?id=com.dayaonweb.quoter"
            )
            putExtra(Intent.EXTRA_STREAM, fileUri)
            data = fileUri
            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
                screenshotView.let { containerView ->
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
                val currentQuote = authorToQuote.values.toTypedArray().getOrNull(newVal) ?: return@setOnValueChangedListener
                quoteTextView.text = currentQuote.quote
                ssQuoteTextView.text = currentQuote.quote
                authorTextView.text = currentQuote.author
                ssAuthorTextView.text = currentQuote.author
                serialTextView.text = String.format("%s", "$currentQuoteNumber/$currentPageCount")
                fadeInViews()
                quoteTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if ((currentQuote.quote.length) > 150) 24f else 32f
                )
                ssQuoteTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if ((currentQuote.quote.length) > 150) 18f else 28f
                )
                if (currentPageCount - (newVal + 1) <= 4) {
                    pageToFetch++
                    if (pageToFetch <= totalPages && !viewModel.isFetchingQuotes)
                        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
                }

                /**************ANALYTICS********************/
                currentQuoteId = currentQuote.id
                currentQuoteTag = listOf(currentQuote.tags.joinToString())
            }

            speakImageView.apply {
                setBackgroundResource(R.drawable.speaking_anim)
                speakerAnimation = background as AnimationDrawable
                setOnClickListener {
                    val uiQuote = authorToQuote.get(bi?.authorTextView?.text?.toString())?: return@setOnClickListener
                    quoteSpeaker.get().speak(uiQuote)
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
            displayedValues = Array(authorToQuote.size) { "" }
        }
        val uiQuote = authorToQuote.values.firstOrNull() ?: return
        bi?.quoteTextView?.text = uiQuote.quote
        bi?.ssQuoteTextView?.text = uiQuote.quote
        bi?.authorTextView?.text = uiQuote.author
        bi?.ssAuthorTextView?.text = uiQuote.author
        bi?.serialTextView?.text = String.format("%s", "$currentQuoteNumber/$currentPageCount")
        bi?.shareImageView?.isVisible = true
        bi?.backImageView?.isVisible = true
        bi?.quoteImageView?.isVisible = true
        bi?.optionsImageView?.isVisible = true
        bi?.speakImageView?.isVisible = true
        bi?.loader?.isVisible = false

        /**************ANALYTICS********************/
        currentQuoteId = uiQuote.id
        currentQuoteTag = uiQuote.tags
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

    override fun onDestroyView() {
        bi = null
        quoteSpeaker.get().stopSpeaking()
        super.onDestroyView()
    }

}