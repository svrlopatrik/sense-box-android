package sk.kotlin.sensebox.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.MainActivityViewModel
import sk.kotlin.sensebox.ui.activities.BaseActivity
import sk.kotlin.sensebox.ui.fragments.history.HistoryFragment
import sk.kotlin.sensebox.ui.fragments.live.LiveFragment
import sk.kotlin.sensebox.ui.fragments.settings.SettingsFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class MainActivity : BaseActivity<MainActivityViewModel>(MainActivityViewModel::class.java) {

    private lateinit var navigationPagerAdapter: NavigationPagerAdapter

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    override fun setLayout() = R.layout.activity_main

    override fun initViews(savedInstanceState: Bundle?) {
        initViewPager()
        initBottomNavigation()
    }

    private fun initViewPager() {
        val fragments = arrayOf<Fragment>(
                LiveFragment.getFragment(),
                HistoryFragment.getFragment(),
                SettingsFragment.getFragment()
        )

        navigationPagerAdapter = NavigationPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = navigationPagerAdapter
        view_pager.offscreenPageLimit = fragments.size
    }

    private fun initBottomNavigation() {
        bottom_navigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_live -> view_pager.setCurrentItem(0, true)
                R.id.action_history -> view_pager.setCurrentItem(1, true)
                R.id.action_settings -> view_pager.setCurrentItem(2, true)
            }
            true
        }
    }

}
