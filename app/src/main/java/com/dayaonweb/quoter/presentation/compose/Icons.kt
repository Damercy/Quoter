package com.dayaonweb.quoter.presentation.compose

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dayaonweb.quoter.R

/** SVG paths are compiled to native animated vectors, crisp at any density. */
@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
internal fun LineIcon(kind: String, description: String? = null, filled: Boolean = false) {
    val animated = when (kind) {
        "search" -> R.drawable.anim_search
        "share" -> R.drawable.anim_share
        "bookmark" -> R.drawable.anim_bookmark
        "speaker" -> R.drawable.anim_speaker
        "arrow" -> R.drawable.anim_arrow
        "back" -> R.drawable.anim_back
        "close" -> R.drawable.anim_close
        "sun" -> R.drawable.anim_sun
        "moon" -> R.drawable.anim_moon
        "system" -> R.drawable.anim_system
        else -> R.drawable.anim_settings
    }
    val static = when (kind) {
        "search" -> R.drawable.vec_search
        "share" -> R.drawable.vec_share
        "bookmark" -> R.drawable.vec_bookmark
        "speaker" -> R.drawable.vec_speaker
        "arrow" -> R.drawable.vec_arrow
        "back" -> R.drawable.vec_back
        "close" -> R.drawable.vec_close
        "sun" -> R.drawable.vec_sun
        "moon" -> R.drawable.vec_moon
        "system" -> R.drawable.vec_system
        else -> R.drawable.vec_settings
    }
    val motion = rememberMotionEnabled()
    val pressed = LocalIconPressed.current
    val activation = LocalIconActivation.current
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(activation, motion) {
        if (activation > 0 && motion) {
            pulse.animateTo(0.65f, tween(110))
            pulse.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 500f))
        } else pulse.snapTo(0f)
    }
    val painter = if (motion) rememberAnimatedVectorPainter(AnimatedImageVector.animatedVectorResource(animated), atEnd = pressed)
        else painterResource(static)
    Crossfade(kind to filled, animationSpec = tween(if (motion) 140 else 0), label = "Icon morph") { (displayKind, selected) ->
        val resource = when (displayKind) {
            "search" -> R.drawable.vec_search; "share" -> R.drawable.vec_share
            "bookmark" -> if (selected) R.drawable.vec_bookmark_filled else R.drawable.vec_bookmark
            "speaker" -> R.drawable.vec_speaker; "close" -> R.drawable.vec_close
            "arrow" -> R.drawable.vec_arrow; "back" -> R.drawable.vec_back
            "sun" -> R.drawable.vec_sun; "moon" -> R.drawable.vec_moon
            "system" -> R.drawable.vec_system; else -> R.drawable.vec_settings
        }
        Icon(if (displayKind == kind && !(kind == "bookmark" && selected)) painter else painterResource(resource),
            contentDescription = if (displayKind == kind && selected == filled) description else null, modifier = Modifier.size(20.dp).graphicsLayer {
                val amount = pulse.value
                scaleX = 1f + amount * if (kind == "bookmark") 0.1f else 0.04f
                scaleY = scaleX
                rotationZ = amount * when (kind) { "settings" -> 4f; "close" -> 20f; "speaker" -> -3f; "share" -> -4f; "sun", "system" -> 8f; else -> 0f }
                translationX = amount * when (kind) { "arrow" -> 3.dp.toPx(); "back" -> -3.dp.toPx(); else -> 0f }
            })
    }
}
