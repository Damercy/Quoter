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
import com.dayaonweb.quoter.extensions.showSnack
import com.dayaonweb.quoter.service.model.Data
import com.dayaonweb.quoter.tts.Quoter
import java.io.File
import java.util.*

class BrowseTag : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var bi: FragmentBrowseTagBinding? = null
    private val viewModel: BrowseTagViewModel by viewModels()
    private var quoteToAuthor = mutableMapOf<Data, String>()
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
        attachListeners()
        attachObservers()
        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
        viewModel.getAllPreferences(requireContext())
    }

    private fun initQuoter(ttsLanguage: Locale) {
        bi?.ttsLoader?.isVisible = true
        quoterSpeaker = Quoter(context = requireContext()) { initStatus ->
            bi?.ttsLoader?.isVisible = false
            if (initStatus == TextToSpeech.SUCCESS) {
                bi?.speakImageView?.isVisible = true
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

                    override fun onError(utteranceId: String, errorCode: Int) {
                        requireActivity().runOnUiThread {
                            speakerAnimation.stop()
                            speakerAnimation.selectDrawable(0)
                            showSnack("Unable to synthesize text")
                        }
                    }
                })
                quoterSpeaker.setEngineLocale(ttsLanguage)
                quoterSpeaker.setSpeechRateSpeed(viewModel.preferences.value?.speechRate ?: 1.0f)
                val engineLocale = quoterSpeaker.getCurrentVoice()?.locale ?: ttsLanguage
                viewModel.updateTtsLanguage(requireContext(), engineLocale)
            } else if (initStatus == TextToSpeech.ERROR) {
                showSnack("Unable to initialize text to speech")
                bi?.speakImageView?.isVisible = false
            }
        }
    }

    private fun attachObservers() {
        viewModel.preferences.observe(viewLifecycleOwner) {
            initQuoter(it.ttsLanguage)
        }
        viewModel.quotes.observe(viewLifecycleOwner) {
            currentPageCount += it.size
            totalPages = it.size
            for (quote in it) {
                quoteToAuthor[quote] = quote.quoteAuthor?:""
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
                val currentQuote = quoteToAuthor.keys.toTypedArray()[newVal]
                quoteTextView.text = currentQuote.quoteText
                ssQuoteTextView.text = currentQuote.quoteText
                authorTextView.text = quoteToAuthor.values.toTypedArray()[newVal]
                ssAuthorTextView.text = quoteToAuthor.values.toTypedArray()[newVal]
                serialTextView.text = String.format("%s", "$currentQuoteNumber/$currentPageCount")
                fadeInViews()
                quoteTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if ((currentQuote.quoteText?.length ?: 0) > 150) 24f else 32f
                )
                ssQuoteTextView.setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    if ((currentQuote.quoteText?.length ?: 0) > 150) 18f else 28f
                )
                if (currentPageCount - (newVal + 1) <= 4) {
                    pageToFetch++
                    if (pageToFetch <= totalPages && !viewModel.isFetchingQuotes)
                        viewModel.fetchQuotesByTag(args.tag, pageToFetch)
                }

                /**************ANALYTICS********************/
                currentQuoteId = currentQuote.id ?: UUID.randomUUID().toString()
                currentQuoteTag = listOf(currentQuote.quoteGenre ?: "")
            }

            speakImageView.apply {
                setBackgroundResource(R.drawable.speaking_anim)
                speakerAnimation = background as AnimationDrawable
                setOnClickListener {
                    if (::quoterSpeaker.isInitialized)
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
        bi?.quoteTextView?.text = quoteToAuthor.keys.toTypedArray()[0].quoteText
        bi?.ssQuoteTextView?.text = quoteToAuthor.keys.toTypedArray()[0].quoteText
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
        currentQuoteId = quoteToAuthor.keys.toTypedArray()[0].id?:UUID.randomUUID().toString()
        currentQuoteTag = listOf(quoteToAuthor.keys.toTypedArray()[0].quoteGenre?:"")
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

    override fun onDestroyView() {
        super.onDestroyView()
        if (::quoterSpeaker.isInitialized)
            quoterSpeaker.deInit()
    }

    companion object {
        private const val TAG = "BrowseTag"
    }

}