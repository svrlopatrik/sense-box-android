package sk.kotlin.sensebox.utils

import android.os.Handler
import android.os.Looper
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun Any.runOnUiThread(function: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        function()
    }
}