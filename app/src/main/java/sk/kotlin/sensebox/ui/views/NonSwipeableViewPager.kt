package sk.kotlin.sensebox.ui.views

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class NonSwipeableViewPager : ViewPager {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent?) = false
    override fun onTouchEvent(ev: MotionEvent?) = false
    override fun executeKeyEvent(event: KeyEvent) = false
}