package com.dayaonweb.quoter.presentation.compose

import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.domain.models.UiQuote
import com.dayaonweb.quoter.domain.broadcast.ReminderScheduler
import com.dayaonweb.quoter.domain.broadcast.ReminderTime
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.Manifest
import android.os.Build
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.ClipEntry
import android.content.ClipData
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlin.math.absoluteValue
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.collectFoldingFeaturesAsState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalView

internal val quoterFont = FontFamily(Font(R.font.main_regular), Font(R.font.main_bold, FontWeight.Bold))
@Serializable internal data object HomeRoute : NavKey
@Serializable internal data object SettingsRoute : NavKey
@Serializable internal data object BrowseRoute : NavKey
data class QuoterActions(val save: (String, Boolean) -> Unit = { _, _ -> }, val theme: (String) -> Unit = {},
    val reminder: (Boolean) -> Unit = {}, val time: (Int, Int) -> Unit = { _, _ -> },
    val imageNotification: (Boolean) -> Unit = {}, val rate: (Float) -> Unit = {}, val voice: (String, String) -> Unit = { _, _ -> },
    val glass: (Boolean) -> Unit = {}, val glassTransparency: (Float) -> Unit = {}, val reviewRequests: (Boolean) -> Unit = {})

internal fun palette(dark: Boolean): ColorScheme {
    val paper = if (dark) Color(0xFF121212) else Color.White
    val ink = if (dark) Color.White else Color.Black
    val muted = if (dark) Color(0xFFB0B0B0) else Color(0xFF626262)
    val raised = if (dark) Color(0xFF202020) else Color(0xFFF4F4F4)
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(primary = ink, onPrimary = paper, secondary = ink, onSecondary = paper,
        tertiary = ink, onTertiary = paper, primaryContainer = raised, onPrimaryContainer = ink,
        secondaryContainer = ink, onSecondaryContainer = paper, tertiaryContainer = raised, onTertiaryContainer = ink,
        error = ink, onError = paper, errorContainer = raised, onErrorContainer = ink, background = paper, onBackground = ink,
        surface = paper, onSurface = ink, surfaceVariant = raised, onSurfaceVariant = muted,
        surfaceContainer = raised, surfaceContainerHigh = raised, surfaceContainerLow = raised,
        surfaceContainerHighest = raised, surfaceContainerLowest = paper, surfaceTint = Color.Transparent,
        surfaceBright = paper, surfaceDim = raised, inverseSurface = ink, inverseOnSurface = paper, inversePrimary = paper,
        outline = muted, outlineVariant = if (dark) Color(0xFF383838) else Color(0xFFE0E0E0))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun QuoterScreen(initialState: QuoterState, initialActions: QuoterActions = QuoterActions()) {
    // NavEntry retains content for a stable route. Read current values through snapshot state.
    val state by rememberUpdatedState(initialState)
    val actions by rememberUpdatedState(initialActions)
    val context = LocalContext.current
    QuietTapSounds()
    val quotes by rememberUpdatedState(state.quotes)
    val saved by rememberUpdatedState(state.saved)
    val theme by rememberUpdatedState(state.settings.theme)
    var glassTransparency by rememberSaveable(state.settings.glassTransparency) { mutableFloatStateOf(state.settings.glassTransparency) }
    var savedOnly by rememberSaveable { mutableStateOf(false) }
    val backStack = rememberNavBackStack(HomeRoute)
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val foldingFeatures by collectFoldingFeaturesAsState()
    val windowWidth = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp() }
    val wide by rememberUpdatedState(windowWidth >= 720.dp || adaptiveInfo.windowPosture.isTabletop || foldingFeatures.any {
        it.isSeparating && it.orientation == androidx.window.layout.FoldingFeature.Orientation.VERTICAL
    })
    val directive = remember(adaptiveInfo, wide) {
        calculatePaneScaffoldDirective(adaptiveInfo).let {
            if (wide && !adaptiveInfo.windowPosture.isTabletop) it.copy(maxHorizontalPartitions = 2) else it
        }
    }
    val supportingStrategy = rememberSupportingPaneSceneStrategy<NavKey>(directive = directive,
        backNavigationBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange)
    val settings = backStack.lastOrNull() == SettingsRoute
    fun closeSettings() { if (backStack.size > 1) backStack.removeLastOrNull() }
    fun openPane(route: NavKey) {
        if (backStack.lastOrNull() != route) {
            while (backStack.size > 1) backStack.removeLastOrNull()
            backStack.add(route)
        }
    }
    var browse by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(wide) {
        if (wide && browse) { browse = false; if (BrowseRoute !in backStack) backStack.add(BrowseRoute) }
        else if (!wide && backStack.lastOrNull() == BrowseRoute) { backStack.removeLastOrNull(); browse = true }
    }
    var timeSheet by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("All") }
    var collectionIds by rememberSaveable { mutableStateOf<List<String>?>(null) }
    val collectionIdSet by rememberUpdatedState(remember(collectionIds) { collectionIds?.toHashSet() })
    var quotesSelectedId by rememberSaveable { mutableStateOf(quotes.firstOrNull()?.id ?: "") }
    var savedSelectedId by rememberSaveable { mutableStateOf("") }
    var speechNotice by remember { mutableStateOf(false) }
    var voiceDialog by remember { mutableStateOf(false) }
    var shareBusy by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) actions.reminder(true) else notice = "Notifications were not enabled. You can allow them in Android Settings."
    }
    val preview = LocalInspectionMode.current
    val voice = remember(context, preview) { QuoteVoice(context, initialize = !preview) }
    var resumeCount by remember { mutableIntStateOf(0) }
    val voiceStatus by voice.status.collectAsStateWithLifecycle()
    LaunchedEffect(state.settings) { voice.configure(state.settings) }
    DisposableEffect(voice) {
        val lifecycle = (context as? ComponentActivity)?.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) voice.stop()
            if (event == Lifecycle.Event.ON_RESUME) resumeCount++
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer); voice.close() }
    }
    val dark = theme == "Dark" || (theme == "System" && isSystemInDarkTheme())
    SideEffect {
        (context as? ComponentActivity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !dark; isAppearanceLightNavigationBars = !dark
            }
        }
    }
    fun toggleSaved(id: String) { actions.save(id, id !in saved) }
    val motion = rememberMotionEnabled()
    val browseScale by androidx.compose.animation.core.animateFloatAsState(if (browse && motion) 0.985f else 1f,
        spring(dampingRatio = 0.9f, stiffness = 400f), label = "Browse presentation")
    var chromeScrolled by remember { mutableStateOf(false) }
    var chromeHeight by remember { mutableIntStateOf(0) }
    val chromeInset = if (chromeHeight > 0) with(LocalDensity.current) { chromeHeight.toDp() } else 64.dp
    LaunchedEffect(settings) { chromeScrolled = false }
    val chromeTitleOpacity by androidx.compose.animation.core.animateFloatAsState(if (chromeScrolled) 1f else 0f,
        tween(if (motion) 160 else 0), label = "Compact title reveal")
    QuoterTheme(theme) {
        val paper = MaterialTheme.colorScheme.surface
        val baseBackdrop = rememberCanvasBackdrop { drawRect(paper) }
        val chromeContent = rememberLayerBackdrop {
            val content = this
            clipRect(bottom = chromeHeight.toFloat() + 32.dp.toPx()) { content.drawContent() }
        }
        val chromeBackdrop = rememberCombinedBackdrop(baseBackdrop, chromeContent)
        CompositionLocalProvider(LocalChromeInset provides chromeInset, LocalChromeScrolled provides { chromeScrolled = it },
            LocalGlassBackdrop provides baseBackdrop,
            LocalGlassStyle provides GlassStyle(state.settings.glassEnabled, glassTransparency)) {
        Surface(Modifier.fillMaxSize()) {
            HingeSafeRegion(useWholeWindow = wide && (BrowseRoute in backStack || SettingsRoute in backStack), features = foldingFeatures) {
            Box(Modifier.safeDrawingPadding()) {
                Box(Modifier.fillMaxSize().graphicsLayer { scaleX = browseScale; scaleY = browseScale }.clipToBounds().then(if (chromeScrolled && state.settings.glassEnabled) Modifier.layerBackdrop(chromeContent) else Modifier)) {
                SharedTransitionLayout(Modifier.fillMaxSize()) {
                NavDisplay(modifier = Modifier.fillMaxSize(), sharedTransitionScope = this, backStack = backStack, onBack = { closeSettings() }, sceneStrategies = listOf(supportingStrategy),
                    transitionSpec = { if (!motion) EnterTransition.None.togetherWith(ExitTransition.None) else
                        slideInHorizontally(tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it }
                        .togetherWith(slideOutHorizontally(tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { -it / 4 })
                        .apply { targetContentZIndex = 1f } },
                    popTransitionSpec = { if (!motion) EnterTransition.None.togetherWith(ExitTransition.None) else
                        slideInHorizontally(tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { -it / 4 }
                        .togetherWith(slideOutHorizontally(tween(240, easing = androidx.compose.animation.core.FastOutSlowInEasing)) { it })
                        .apply { targetContentZIndex = -1f } },
                    entryProvider = { route -> NavEntry(route, metadata = when (route) {
                        HomeRoute -> SupportingPaneSceneStrategy.mainPane()
                        BrowseRoute -> SupportingPaneSceneStrategy.supportingPane()
                        SettingsRoute -> if (wide) SupportingPaneSceneStrategy.supportingPane() else emptyMap()
                        else -> emptyMap()
                    }) {
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).clipToBounds()) {
                if (route == BrowseRoute) {
                    CompositionLocalProvider(LocalChromeInset provides 0.dp) {
                    BrowsePane(quotes, query, { query = it }, topic, { topic = it }, onClose = { closeSettings() }) { quote, results ->
                        savedOnly = false; collectionIds = results.map { it.id }; quotesSelectedId = quote.id; voice.stop()
                    }
                    }
                } else if (route == SettingsRoute) {
                    SettingsPane(wide, onClose = { closeSettings() }) {
                    val notificationsAllowed = remember(resumeCount, state.settings.reminders) { ReminderScheduler(context).canNotify() }
                    SettingsContent(state.settings, actions, glassTransparency, { glassTransparency = it },
                        onVoice = { voiceDialog = true },
                        onReminder = { enabled ->
                            if (!enabled) actions.reminder(false)
                            else if (Build.VERSION.SDK_INT >= 33 && !ReminderScheduler(context).canNotify()) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            else actions.reminder(true)
                        }, notificationStatus = if (!state.settings.reminders) "Reminders are off" else if (notificationsAllowed) "Around ${state.settings.time} · timing may vary with battery settings" else "Notifications are blocked in Android Settings",
                        onNotificationSettings = {
                            val intent = if (Build.VERSION.SDK_INT >= 26) Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                else Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        }, onTime = { timeSheet = true })
                    }
                } else {
                    AnimatedContent(savedOnly, transitionSpec = {
                        if (!motion) EnterTransition.None.togetherWith(ExitTransition.None) else
                            (fadeIn(tween(140, delayMillis = 40)) + slideInHorizontally(tween(200)) { if (targetState) it / 16 else -it / 16 })
                                .togetherWith(fadeOut(tween(80))).using(SizeTransform(clip = true))
                    }, label = "Reader collection", modifier = Modifier.fillMaxSize()) { showSaved ->
                    val visibleCollection = if (showSaved) quotes.filter { it.id in saved } else if (collectionIdSet == null) quotes else quotes.filter { it.id in collectionIdSet!! }
                    if (visibleCollection.isEmpty()) EmptyQuoteReader(showSaved) {
                        if (wide) openPane(BrowseRoute) else browse = true
                    } else key(showSaved, visibleCollection.map { it.id }) {
                        QuoteReader(visibleCollection, if (showSaved) savedSelectedId else quotesSelectedId, saved, voiceStatus, shareBusy,
                            onPage = { if (showSaved) savedSelectedId = it else quotesSelectedId = it; voice.stop() },
                            onSave = { toggleSaved(it.id) },
                            onListen = { if (voiceStatus == "Listen" || voiceStatus == "Stop" || voiceStatus.startsWith("Speech failed")) voice.toggle(it) else speechNotice = true },
                            onShare = { quote ->
                                voice.stop()
                                if (!shareBusy) {
                                    shareBusy = true
                                    scope.launch {
                                        try {
                                            val intent = withContext(Dispatchers.IO) { QuoteImage.export(context, quote, dark) }
                                            context.startActivity(Intent.createChooser(intent, "Share quote"))
                                        } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
                                        catch (_: Exception) { notice = "Could not create the quote image. Please try again." }
                                        finally { shareBusy = false }
                                    }
                                }
                            })
                    } }
                } } } })
                }
                }
                HingeSafeRegion(useWholeWindow = false, features = foldingFeatures) {
                AnimatedContent(settings && !wide, modifier = Modifier.then(if (wide && !adaptiveInfo.windowPosture.isTabletop) Modifier.width(windowWidth / 2) else Modifier.fillMaxWidth()).onSizeChanged { chromeHeight = it.height }.chromeSurface(chromeBackdrop, chromeScrolled),
                    transitionSpec = { fadeIn(tween(if (motion) 140 else 0, delayMillis = if (motion) 80 else 0))
                        .togetherWith(fadeOut(tween(if (motion) 80 else 0))).using(SizeTransform(clip = false)) }, label = "Navigation header") { showingSettings ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (showingSettings) {
                        GlassIconButton(onClick = { closeSettings() }) { LineIcon("back", "Back to quotes") }
                        Text("Settings", Modifier.weight(1f).graphicsLayer { alpha = chromeTitleOpacity }, textAlign = TextAlign.Center, fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Spacer(Modifier.width(48.dp))
                    } else {
                        CollectionTabs(savedOnly, onQuotes = { savedOnly = false; collectionIds = null; voice.stop() },
                            onSaved = { savedOnly = true; voice.stop() }, modifier = Modifier.weight(1f).wrapContentWidth(Alignment.Start))
                        GlassIconButton(enabled = !browse, modifier = Modifier.graphicsLayer { alpha = if (browse) 0f else 1f }, onClick = {
                            if (wide) { openPane(BrowseRoute) } else browse = true
                            voice.stop()
                        }) { if (!browse) LineIcon("search", "Browse quotes") }
                        GlassIconButton(onClick = { browse = false; openPane(SettingsRoute); voice.stop() }) { LineIcon("settings", "Settings") }
                    }
                }
                }
                }
            }
            }
        }
        val browseSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        fun dismissBrowse() { scope.launch { browseSheetState.hideThroughResize(); browse = false } }
        if (browse) CompositionLocalProvider(LocalChromeInset provides 0.dp, LocalChromeScrolled provides {}) { ModalBottomSheet(onDismissRequest = { browse = false }, containerColor = MaterialTheme.colorScheme.surface, sheetState = browseSheetState) {
            QuietTapSounds()
            Box(Modifier.fillMaxWidth().fillMaxHeight(0.85f)) {
                BrowsePane(quotes, query, { query = it }, topic, { topic = it }) { quote, results ->
                    savedOnly = false; collectionIds = results.map { it.id }; quotesSelectedId = quote.id; dismissBrowse()
                }
            }
        }
        }
        if (timeSheet) {
            val (hour, minute) = ReminderTime.parse(state.settings.time)
            val picker = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = DateFormat.is24HourFormat(context))
            QuoterSheet(onDismissRequest = { timeSheet = false }, title = { Text("Reminder time") }, text = {
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (maxWidth < 300.dp || maxHeight < 380.dp) TimeInput(picker)
                    else TimePicker(picker, layoutType = TimePickerLayoutType.Vertical)
                }
            }, confirmButton = {}, onConfirm = { actions.time(picker.hour, picker.minute); timeSheet = false }, cancelable = true)
        }
        if (speechNotice) QuoterSheet(onDismissRequest = { speechNotice = false }, title = { Text("Listen") },
            text = { Text(voiceStatus) }, confirmButton = { MicroTextButton(onClick = { speechNotice = false }) { Text("OK") } })
        if (voiceDialog) QuoterSheet(onDismissRequest = { voiceDialog = false }, title = { Text("Offline voices") }, text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                item { MicroTextButton(onClick = { actions.voice("", state.settings.locale); voiceDialog = false }) { Text("Automatic") } }
                items(voice.availableVoices, key = { it.name }) { option ->
                    MicroTextButton(onClick = { actions.voice(option.name, option.locale.toLanguageTag()); voiceDialog = false }) {
                        Text("${option.locale.displayName}\n${option.name}")
                    }
                }
                if (voice.availableVoices.isEmpty()) item { QuietEmptyState("speaker", "No offline voices", "Install a voice in your device’s speech settings.") }
            }
        }, confirmButton = { MicroTextButton(onClick = { voiceDialog = false }) { Text("Close") } })
        notice?.let { message -> QuoterSheet(onDismissRequest = { notice = null }, title = { Text("Quoter") }, text = { Text(message) },
            confirmButton = { MicroTextButton(onClick = { notice = null }) { Text("OK") } }) }
        }
    }
}

@Composable
private fun QuoteReader(quotes: List<UiQuote>, selectedId: String, saved: Set<String>, voiceStatus: String, shareBusy: Boolean,
    onPage: (String) -> Unit, onSave: (UiQuote) -> Unit, onListen: (UiQuote) -> Unit, onShare: (UiQuote) -> Unit) {
    val pager = rememberPagerState(initialPage = quotes.indexOfFirst { it.id == selectedId }.coerceAtLeast(0), pageCount = { quotes.size })
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager.settledPage) { onPage(quotes[pager.settledPage].id) }
    val haptic = rememberQuoterHaptic()
    val dragged by pager.interactionSource.collectIsDraggedAsState()
    var userPaging by remember { mutableStateOf(false) }
    var lastSettled by remember { mutableIntStateOf(pager.settledPage) }
    LaunchedEffect(dragged) { if (dragged) userPaging = true }
    LaunchedEffect(pager.settledPage) {
        if (lastSettled != pager.settledPage) {
            if (userPaging) haptic(true)
            userPaging = false
            lastSettled = pager.settledPage
        }
    }
    LaunchedEffect(selectedId) {
        val index = quotes.indexOfFirst { it.id == selectedId }
        if (index >= 0 && index != pager.settledPage && !pager.isScrollInProgress) pager.scrollToPage(index)
    }
    val current = quotes[pager.settledPage]
    val quoteBackdrop = rememberLayerBackdrop { drawContent() }
    val baseBackdrop = requireNotNull(LocalGlassBackdrop.current)
    val controlsBackdrop = rememberCombinedBackdrop(baseBackdrop, quoteBackdrop)
    val motionEnabled = rememberMotionEnabled()
    val motionBlur = rememberMotionBlur()
    val density = LocalDensity.current
    var footerHeight by remember { mutableIntStateOf(0) }
    val chromeInset = LocalChromeInset.current
    val onScrolled = LocalChromeScrolled.current
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(state = pager, key = { quotes[it].id }, modifier = Modifier.fillMaxSize().layerBackdrop(quoteBackdrop).padding(bottom = maxOf(84.dp, with(density) { footerHeight.toDp() }))) { page ->
            Box(Modifier.fillMaxSize().padding(horizontal = 28.dp).graphicsLayer {
                val distance = if (motionEnabled) ((pager.currentPage - page) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f,1f) else 0f
                scaleX = 1f - distance * 0.015f; scaleY = scaleX; alpha = 1f - distance * 0.22f
                renderEffect = motionBlur[(distance * 4).toInt().coerceIn(0, 4)]
            }, contentAlignment = Alignment.Center) {
                val readerScroll = rememberScrollState()
                LaunchedEffect(readerScroll.value, pager.settledPage) {
                    if (page == pager.settledPage) onScrolled(readerScroll.value > 0)
                }
                Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(readerScroll)
                    .padding(top = chromeInset + 24.dp, bottom = 24.dp)) {
                    Text("“", fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = 52.sp, lineHeight = 52.sp)
                    Text(quotes[page].quote, fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 34.sp)
                    Text(quotes[page].author.uppercase(Locale.ROOT), modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                        textAlign = TextAlign.End, fontFamily = quoterFont, fontSize = 14.sp, lineHeight = 20.sp)
                }
            }
        }
        CompositionLocalProvider(LocalGlassBackdrop provides controlsBackdrop) {
        FlowRow(Modifier.align(Alignment.BottomCenter).fillMaxWidth().onSizeChanged { footerHeight = it.height }.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
            GlassIconButton(enabled = !shareBusy, onClick = { onShare(current) }) { LineIcon("share", "Share quote") }
            GlassIconButton(onClick = { onListen(current) }) { LineIcon(if (voiceStatus == "Stop") "close" else "speaker", if (voiceStatus == "Stop") "Stop reading" else "Listen to quote") }
            GlassIconButton(onClick = { onSave(current) }) { LineIcon("bookmark", if (current.id in saved) "Unsave quote" else "Save quote", current.id in saved) }
            }
            QuoteCounter(pager.settledPage + 1, quotes.size, Modifier.heightIn(min = 48.dp).padding(end = 8.dp))
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuotePreview() { QuoterPreviewContent(QuoterState(quotes = listOf(UiQuote("preview", "Stay hungry, stay foolish.", "Steve Jobs", listOf("inspiration"))))) }


