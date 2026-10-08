package com.dayaonweb.quoter.presentation.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(Locale.ROOT), Modifier.padding(horizontal = 16.dp), fontFamily = quoterFont,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer), content = content)
    }
}

@Composable
private fun SettingsDivider() { HorizontalDivider(Modifier.padding(start = 16.dp), thickness = 0.5.dp) }

@Composable
private fun SettingsSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val haptic = rememberQuoterHaptic()
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontFamily = quoterFont, fontSize = 16.sp)
        Switch(checked, { haptic(true); onChange(it) }, Modifier.semantics { contentDescription = label })
    }
}

@Composable
private fun SettingsLink(label: String, value: String = "", onClick: () -> Unit) {
    val haptic = rememberQuoterHaptic()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val alpha by animateFloatAsState(if (pressed && rememberMotionEnabled()) 0.6f else 1f, spring(stiffness = 700f), label = "Settings row press")
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha }
        .clickable(interactionSource = interaction, indication = LocalIndication.current) { haptic(false); onClick() }.padding(16.dp)) {
        val stackValue = value.isNotEmpty() && (fontScale > 1.3f || maxWidth < 280.dp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (stackValue) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(label, fontFamily = quoterFont, fontSize = 16.sp)
                    Text(value, fontFamily = quoterFont, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                }
            } else {
                Text(label, Modifier.weight(1f), fontFamily = quoterFont, fontSize = 16.sp)
                if (value.isNotEmpty()) Text(value, Modifier.widthIn(max = 170.dp), fontFamily = quoterFont,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Original monochrome typography with grouped rows and content that scrolls beneath the chrome. */
@Composable
internal fun SettingsContent(settings: AppSettings, actions: QuoterActions, transparency: Float,
    onTransparency: (Float) -> Unit, onVoice: () -> Unit,
    onReminder: (Boolean) -> Unit, notificationStatus: String, onNotificationSettings: () -> Unit, onTime: () -> Unit) {
    val scroll = rememberScrollState()
    val onScrolled = LocalChromeScrolled.current
    val inset = LocalChromeInset.current
    var titleHeight by remember { mutableIntStateOf(0) }
    val titleGap = with(LocalDensity.current) { 20.dp.roundToPx() }
    LaunchedEffect(scroll.value, titleHeight) { onScrolled(titleHeight > 0 && scroll.value > titleHeight + titleGap) }
    val systemDark = isSystemInDarkTheme()
    Column(Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = 640.dp)
        .verticalScroll(scroll).padding(top = inset, bottom = 24.dp).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Settings", Modifier.onSizeChanged { titleHeight = it.height }.padding(start = 4.dp, top = 8.dp, bottom = 4.dp), fontFamily = quoterFont,
            fontWeight = FontWeight.Bold, fontSize = 28.sp)
        SettingsSection("Appearance") {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                AppearancePreview("Light", settings.theme == "Light", false) { actions.theme("Light") }
                AppearancePreview("Dark", settings.theme == "Dark", true) { actions.theme("Dark") }
            }
            SettingsDivider()
            SettingsSwitch("Automatic", settings.theme == "System") {
                actions.theme(if (it) "System" else if (systemDark) "Dark" else "Light")
            }
            SettingsDivider()
            SettingsSwitch("Glass appearance", settings.glassEnabled, actions.glass)
            SettingsDivider()
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                SliderLabel("Transparency", "${kotlin.math.round(transparency * 100).toInt()}%")
                WavySlider(transparency, onTransparency, { actions.glassTransparency(transparency) }, "Glass transparency", enabled = settings.glassEnabled)
            }
        }
        SettingsSection("Voice") {
            SettingsLink("Language", Locale.forLanguageTag(settings.locale.replace('_','-')).displayName, onVoice)
            SettingsDivider()
            var speed by rememberSaveable(settings.rate) { mutableFloatStateOf(settings.rate) }
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                SliderLabel("Speech speed", "${String.format(Locale.ROOT, "%.2f", speed)}×")
                WavySlider(speed, { speed = it }, { actions.rate(speed) }, "Speech speed", 0.5f..2f, 5)
            }
        }
        SettingsSection("Notifications") {
            SettingsSwitch("Daily reminder", settings.reminders, onReminder)
            SettingsDivider()
            SettingsLink("Time", settings.time, onTime)
            SettingsDivider()
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Style", Modifier.weight(1f), fontFamily = quoterFont, fontSize = 16.sp)
                listOf(false to "Text", true to "Image").forEach { (image, label) ->
                    MicroTextButton({ actions.imageNotification(image) }, Modifier.semantics { selected = settings.imageNotification == image }) {
                        Text(label, fontWeight = if (settings.imageNotification == image) FontWeight.Bold else FontWeight.Normal,
                            color = if (settings.imageNotification == image) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            NotificationPreview(settings.imageNotification)
            SettingsDivider()
            SettingsLink("Notification settings", onClick = onNotificationSettings)
        }
        Text(notificationStatus, Modifier.padding(horizontal = 16.dp), fontFamily = quoterFont,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
private fun NotificationPreview(image: Boolean) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()
        .clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp)
        .heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("QUOTER · DAILY QUOTE", fontFamily = quoterFont, fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Make room for quiet thinking.", fontFamily = quoterFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text("Notification preview", fontFamily = quoterFont, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        androidx.compose.animation.AnimatedVisibility(image,
            enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(if (rememberMotionEnabled()) 160 else 0)),
            exit = androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(if (rememberMotionEnabled()) 120 else 0))) {
            Box(Modifier.size(52.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceContainer), contentAlignment = Alignment.Center) {
                Text("“", fontFamily = quoterFont, fontWeight = FontWeight.Bold, fontSize = 32.sp)
            }
        }
    }
}

@Composable
private fun SliderLabel(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, Modifier.weight(1f), fontFamily = quoterFont, fontSize = 16.sp)
        Text(value, fontFamily = quoterFont, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}

/** Own screen artwork, rather than Apple-only symbols or branded assets. */
@Composable
private fun AppearancePreview(label: String, selected: Boolean, dark: Boolean, onClick: () -> Unit) {
    val haptic = rememberQuoterHaptic()
    val paper = if (dark) Color(0xFF121212) else Color.White
    val ink = if (dark) Color.White else Color.Black
    Column(Modifier.widthIn(min = 100.dp).selectable(selected = selected, role = Role.RadioButton,
        onClick = { haptic(true); onClick() }).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.size(74.dp, 122.dp).border(2.dp, Color(0xFF626262), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)).background(paper).padding(9.dp)) {
            Box(Modifier.align(Alignment.CenterHorizontally).size(22.dp, 4.dp).background(ink, RoundedCornerShape(50)))
            Spacer(Modifier.height(21.dp))
            Text("“", fontFamily = quoterFont, fontSize = 21.sp, color = ink, lineHeight = 20.sp)
            repeat(3) { index -> Box(Modifier.padding(bottom = 5.dp).fillMaxWidth(if (index == 2) 0.65f else 1f)
                .height(3.dp).background(ink.copy(alpha = 0.85f), RoundedCornerShape(50))) }
            Spacer(Modifier.weight(1f))
            Box(Modifier.align(Alignment.CenterHorizontally).size(22.dp, 3.dp).background(ink.copy(alpha = 0.5f), RoundedCornerShape(50)))
        }
        Text(label, fontFamily = quoterFont, fontSize = 14.sp)
        RadioButton(selected, onClick = null, Modifier.size(24.dp))
    }
}
