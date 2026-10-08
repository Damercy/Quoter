package com.dayaonweb.quoter.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.semantics.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.dayaonweb.quoter.domain.models.UiQuote

@Composable
internal fun BrowsePane(quotes: List<UiQuote>, query: String, onQuery: (String) -> Unit, topic: String,
    onTopic: (String) -> Unit, onClose: (() -> Unit)? = null, onSelect: (UiQuote,List<UiQuote>) -> Unit) {
    val focus = LocalFocusManager.current
    val haptic = rememberQuoterHaptic()
    val motion = rememberMotionEnabled()
    val backdrop = rememberLayerBackdrop { drawContent() }
    val chromeBackdrop = rememberCombinedBackdrop(requireNotNull(LocalGlassBackdrop.current), backdrop)
    var headerHeight by remember { mutableIntStateOf(0) }
    val headerInset = with(LocalDensity.current) { headerHeight.toDp() }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val collapsed by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 48 } }
    val titleSize by animateFloatAsState(if (collapsed) 18f else 28f, tween(if (motion) 180 else 0), label = "Browse title collapse")
    val topics = remember(quotes) { listOf("All") + quotes.flatMap { it.tags }.distinct().sorted() }
    val results = remember(quotes,query,topic) { quotes.filter { (topic == "All" || topic in it.tags) &&
        (it.quote.contains(query,true) || it.author.contains(query,true)) } }
    LaunchedEffect(query, topic) { listState.scrollToItem(0) }
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).imePadding().padding(top = LocalChromeInset.current)) {
        if (results.isEmpty()) Box(Modifier.fillMaxSize().padding(top = headerInset), contentAlignment = Alignment.Center) {
            QuietEmptyState("search", "No quotes found", "Try another word or topic.", "Clear filters") { onQuery(""); onTopic("All"); focus.clearFocus() }
        }
        if (results.isNotEmpty()) LazyColumn(Modifier.fillMaxSize().then(if (collapsed && LocalGlassStyle.current.enabled) Modifier.layerBackdrop(backdrop) else Modifier), state = listState, contentPadding = PaddingValues(top = headerInset, start = 16.dp, end = 16.dp)) {
            items(results, key = { it.id }) { quote ->
                val touch = remember { MutableInteractionSource() }
                val pressed by touch.collectIsPressedAsState()
                val scale by animateFloatAsState(if (pressed && motion) 0.99f else 1f, spring(dampingRatio = 0.8f), label = "Quote press")
                Column(Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale; alpha = if (pressed) 0.7f else 1f }
                    .clickable(interactionSource = touch, indication = androidx.compose.foundation.LocalIndication.current) { haptic(true); focus.clearFocus(); onSelect(quote,results) }.padding(vertical = 20.dp)) {
                    Text(quote.quote, fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 26.sp)
                    Text(quote.author, Modifier.padding(top = 8.dp), fontFamily = quoterFont, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Column(Modifier.fillMaxWidth().onSizeChanged { headerHeight = it.height }
            .chromeSurface(chromeBackdrop, collapsed).padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Browse", Modifier.weight(1f), fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = titleSize.sp, lineHeight = 40.sp)
            if (onClose != null) GlassIconButton(onClick = onClose) { LineIcon("close", "Close browse") }
        }
        val interaction = remember { MutableInteractionSource() }
        val focused by interaction.collectIsFocusedAsState()
        val outline by animateColorAsState(if (focused) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            else androidx.compose.ui.graphics.Color.Transparent, tween(if (motion) 180 else 0), label = "Search focus")
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f), RoundedCornerShape(14.dp))
            .border(0.75.dp, outline, RoundedCornerShape(14.dp)).padding(start = 14.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            LineIcon("search")
            BasicTextField(query, onQuery, singleLine = true, interactionSource = interaction,
                textStyle = TextStyle(fontFamily = quoterFont, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 12.dp)
                    .semantics { contentDescription = "Search quotes or authors" },
                decorationBox = { field -> Box {
                    if (query.isEmpty()) Text("Search quotes or authors", fontFamily = quoterFont, fontSize = 16.sp,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    field()
                } })
            AnimatedVisibility(query.isNotEmpty(), enter = fadeIn(tween(if (motion) 120 else 0)), exit = fadeOut(tween(if (motion) 120 else 0))) {
                GlassIconButton(onClick = { onQuery("") }) { LineIcon("close", "Clear search") }
            }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            items(topics, key = { it }) { tag -> MicroTextButton(onClick = { onTopic(tag) },
                modifier = Modifier.semantics { selected = topic == tag }, pressFeedback = false) {
                val selected = topic == tag
                Text(tag.replaceFirstChar { it.titlecase() }, fontFamily = quoterFont,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                Text(" ·", Modifier.clearAndSetSemantics {}.graphicsLayer { alpha = if (selected) 1f else 0f }, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            } }
        }
        }
    }
}
