package com.dayaonweb.quoter.extensions

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.dayaonweb.quoter.R
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnack(text: String) {
    val snack = Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT)
    val snackView = snack.view
    snackView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
    val snackText =
        snackView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    snackText.typeface = ResourcesCompat.getFont(requireContext(), R.font.main_bold)
    snack.animationMode = Snackbar.ANIMATION_MODE_SLIDE
    snack.show()
}