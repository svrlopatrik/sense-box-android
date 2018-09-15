package sk.kotlin.sensebox.utils

import android.databinding.BindingAdapter
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.widget.ImageView

/**
 * Created by Patrik Švrlo on 9.9.2018.
 */

@BindingAdapter("bind:source", "bind:tintColor")
fun setImageResource(imageView: ImageView, @DrawableRes drawableRes: Int, @ColorRes colorRes: Int) {
    imageView.apply {
        setImageResource(drawableRes)
        setColorFilter(ContextCompat.getColor(context, colorRes), android.graphics.PorterDuff.Mode.SRC_IN)
    }
}