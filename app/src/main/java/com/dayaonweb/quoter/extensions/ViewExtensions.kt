package com.dayaonweb.quoter.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.dayaonweb.quoter.R
import com.google.android.material.snackbar.Snackbar

private const val TAG = "ViewExtensions"

fun ImageView.loadImageUri(url: String?) =
    Glide.with(this).load(url).apply(RequestOptions.centerCropTransform())
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(R.drawable.ic_loop_loading)
        .error(R.drawable.ic_profile)
        .into(this)

fun View.isVisible(visible: Boolean) {
    visibility = if (visible)
        View.VISIBLE
    else
        View.GONE
}

fun View.snack(message: String, duration: Int = Snackbar.LENGTH_SHORT) =
    Snackbar.make(this, message, duration).show()

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    this.requestFocus()
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(windowToken, 0)
}
