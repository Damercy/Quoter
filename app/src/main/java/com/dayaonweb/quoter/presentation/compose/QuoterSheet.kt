package com.dayaonweb.quoter.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.collectFoldingFeaturesAsState

/** Native draggable sheet; choices remain on one usable side of a separating hinge. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun QuoterSheet(onDismissRequest: () -> Unit, title: @Composable () -> Unit,
    text: @Composable () -> Unit, confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null, onConfirm: (() -> Unit)? = null, cancelable: Boolean = false) {
    val features by collectFoldingFeaturesAsState()
    val height = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() * 0.85f }.coerceAtMost(560.dp)
    val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest, containerColor = MaterialTheme.colorScheme.surface, sheetState = sheet) {
        QuietTapSounds()
        Box(Modifier.fillMaxWidth().height(height)) {
        HingeSafeRegion(false, features) {
            BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.TopCenter) {
                    Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().heightIn(max = maxHeight).semantics { paneTitle = "Quoter sheet" }, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (cancelable) MicroTextButton(onClick = { scope.launch { sheet.hideThroughResize(); onDismissRequest() } }) { Text("Cancel") }
                            else dismissButton?.invoke()
                            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                ProvideTextStyle(MaterialTheme.typography.titleMedium.copy(fontFamily = quoterFont, fontSize = 18.sp), title)
                            }
                            if (onConfirm != null) MicroTextButton(onClick = { scope.launch { sheet.hideThroughResize(); onConfirm() } }) { Text("Done") }
                            else confirmButton()
                        }
                        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                            ProvideTextStyle(MaterialTheme.typography.bodyMedium.copy(fontFamily = quoterFont), text)
                        }
                    }
            }
        }
        }
    }
}

/** A keyboard/anchor mutation can cancel an in-flight hide without cancelling the caller. */
@OptIn(ExperimentalMaterial3Api::class)
internal suspend fun SheetState.hideThroughResize() {
    repeat(3) {
        try { hide(); return }
        catch (cancelled: CancellationException) {
            currentCoroutineContext().ensureActive()
            withFrameNanos { }
        }
    }
}
