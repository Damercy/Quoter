package com.dayaonweb.quoter.presentation.view.ui.browsetag

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.domain.analytics.Analytics
import com.dayaonweb.quoter.databinding.FragmentBrowseTagBinding
import com.dayaonweb.quoter.domain.extensions.showSnack
import com.dayaonweb.quoter.domain.tts.QuoteSpeaker
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.carousel.FullScreenCarouselStrategy
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BrowseTag : Fragment(), PopupMenu.OnMenuItemClickListener {

    private var bi: FragmentBrowseTagBinding? = null
    private val viewModel: BrowseTagViewModel by viewModels()

    @Inject
    lateinit var quoteSpeaker: Lazy<QuoteSpeaker>
    private val args: BrowseTagArgs by navArgs()
    private lateinit var speakerAnimation: AnimationDrawable

    private val quotesAdapter by lazy { QuotesAdapter() }
    private val snapHelper by lazy { CarouselSnapHelper() }

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
        setupUi()
        attachListeners()
        attachObservers()
        viewModel.fetchQuotesByTag(args.tag)
    }

    private fun setupUi() {
        bi?.rvQuotes?.apply {
            val manager = CarouselLayoutManager(
                FullScreenCarouselStrategy(),
                CarouselLayoutManager.HORIZONTAL
            )
            layoutManager = manager
            snapHelper.attachToRecyclerView(this)
            adapter = quotesAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    updateSerialText()
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        updateSerialText()
                    }
                }
            })
        }
        bi?.speakImageView?.apply {
            setBackgroundResource(R.drawable.speaking_anim)
            post {
                speakerAnimation = background as AnimationDrawable
            }
        }
    }

    private fun initQuoter(ttsLanguage: Locale) {
        bi?.ttsLoader?.isVisible = false
        bi?.speakImageView?.isVisible = true
        val speaker = quoteSpeaker.get()
        speaker.initSpeakListener()
        speaker.setEngineLocale(ttsLanguage)
        speaker.setSpeechRate(viewModel.preferences.value?.speechRate ?: 1.0f)
        val engineLocale = speaker.getEngineLocale() ?: ttsLanguage
        viewModel.updateTtsLanguage(engineLocale)
    }

    private fun attachObservers() {
        viewModel.preferences.observe(viewLifecycleOwner) {
            initQuoter(it.ttsLanguage)
        }
        viewModel.quotes.observe(viewLifecycleOwner) {
            bi?.apply {
                loader.isVisible = false
                rvQuotes.isVisible = true
                rvQuotes.requestLayout()
                backImageView.isVisible = true
                shareImageView.isVisible = true
                optionsImageView.isVisible = true
                serialTextView.isVisible = true
            }
            quotesAdapter.submitList(it)
            updateSerialText()
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
        quoteSpeaker.get().isSpeaking().asLiveData().observe(viewLifecycleOwner) { isSpeaking ->
            if (::speakerAnimation.isInitialized.not())
                return@observe
            if (isSpeaking) {
                speakerAnimation.start()
            } else {
                speakerAnimation.stop()
                speakerAnimation.selectDrawable(0)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSerialText() {
        val layoutManager = bi?.rvQuotes?.layoutManager as? CarouselLayoutManager ?: return
        val focusedView = snapHelper.findSnapView(layoutManager) ?: layoutManager.focusedChild ?: return
        val current = layoutManager.getPosition(focusedView) + 1
        val total = quotesAdapter.itemCount

        bi?.serialTextView?.text = "$current/$total"
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
                // 1. Get the layout manager from the RecyclerView
                val layoutManager =
                    rvQuotes.layoutManager as? CarouselLayoutManager ?: return@setOnClickListener
                // 2. The CarouselLayoutManager provides the center item index via its offset
                val focusedChildView = snapHelper.findSnapView(layoutManager) ?: layoutManager.focusedChild ?: return@setOnClickListener
                val currentPosition = layoutManager.getPosition(focusedChildView)
                // 3. Find the ViewHolder for that position
                val viewHolder =
                    rvQuotes.findViewHolderForLayoutPosition(currentPosition) as? QuotesAdapter.QuotesViewHolder
                // 4. Access the binding and view
                val quoteRootView = viewHolder?.binding?.quoteRoot ?: return@setOnClickListener
                // 5. Trigger the screenshot
                viewModel.takeScreenShot(
                    quoteRootView,
                    File(quoteRootView.context.externalCacheDir, "quoter_${UUID.randomUUID()}.jpg")
                )
            }
            optionsImageView.setOnClickListener {
                showPopup(it)
            }
            backImageView.setOnClickListener {
                requireActivity().onBackPressed()
            }
            speakImageView.setOnClickListener {
                // 1. Get the layout manager from the RecyclerView
                val layoutManager =
                    rvQuotes.layoutManager as? CarouselLayoutManager ?: return@setOnClickListener
                // 2. The CarouselLayoutManager provides the center item index via its offset
                val focusedChildView = snapHelper.findSnapView(layoutManager) ?:layoutManager.focusedChild ?: return@setOnClickListener
                val currentPosition = layoutManager.getPosition(focusedChildView)
                val uiQuote = quotesAdapter.currentList.getOrNull(currentPosition)
                    ?: return@setOnClickListener
                quoteSpeaker.get().speak(uiQuote)
            }
        }
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
                // 1. Get the layout manager from the RecyclerView
                val layoutManager =
                    bi?.rvQuotes?.layoutManager as? CarouselLayoutManager ?: return false
                // 2. The CarouselLayoutManager provides the center item index via its offset
                val focusedChildView = snapHelper.findSnapView(layoutManager) ?: layoutManager.focusedChild ?: return false
                val currentPosition = layoutManager.getPosition(focusedChildView)
                val uiQuote = quotesAdapter?.currentList?.getOrNull(currentPosition) ?: return false
                val quote = String.format("%s\n\n~ %s", uiQuote.quote, uiQuote.author)
                Analytics.trackQuoteCopy(quote, uiQuote.id, uiQuote.tags)
                copyText(quote)
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

    override fun onStop() {
        quoteSpeaker.get().stopSpeaking()
        if (::speakerAnimation.isInitialized) {
            speakerAnimation.stop()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        bi = null
        quoteSpeaker.get().stopSpeaking()
        if (::speakerAnimation.isInitialized) {
            speakerAnimation.stop()
        }
        super.onDestroyView()
    }

}