package sk.kotlin.sensebox.ui.activities.main

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

/**
 * Created by Patrik Švrlo on 9.9.2018.
 */
class NavigationPagerAdapter(
        fragmentManager: FragmentManager,
        val fragments: Array<Fragment>
) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int) = fragments[position]

    override fun getCount() = fragments.size
}