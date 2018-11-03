package sk.kotlin.sensebox.ui.activities.main

import android.Manifest
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.databinding.ObservableInt
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import sk.kotlin.sensebox.BR
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.MainActivityViewModel
import sk.kotlin.sensebox.models.ui_states.MainActivityState
import sk.kotlin.sensebox.ui.activities.BaseActivity
import sk.kotlin.sensebox.ui.fragments.actual.ActualFragment
import sk.kotlin.sensebox.ui.fragments.history.HistoryFragment
import sk.kotlin.sensebox.ui.fragments.notification.NotificationFragment
import sk.kotlin.sensebox.ui.fragments.settings.SettingsFragment
import sk.kotlin.sensebox.utils.FileLoggingTree
import timber.log.Timber

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class MainActivity : BaseActivity<MainActivityViewModel>(MainActivityViewModel::class.java), EasyPermissions.PermissionCallbacks {

    private lateinit var navigationPagerAdapter: NavigationPagerAdapter

    companion object {
        const val RC_PERMISSIONS = 0x01

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    val bleStateImageSrc = ObservableInt(R.drawable.ic_bluetooth_disconnected)
    val bleStateImageTintColor = ObservableInt(R.color.cc_rd_800)

    override fun setLayout() = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions(RC_PERMISSIONS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding?.apply {
            setVariable(BR.activity, this@MainActivity)
        }

        observeState()
        initViewPager()
        initBottomNavigation()
    }

    private fun observeState() {
        viewModel?.getMainActivityState()?.observe(this, Observer { state -> state?.let { render(it) } })
    }

    private fun render(state: MainActivityState) {
        when (state) {
            is MainActivityState.BleConnecting -> {
                bleStateImageSrc.set(R.drawable.ic_bluetooth_searching)
                bleStateImageTintColor.set(R.color.cc_yw_800)
            }
            is MainActivityState.BleConnected -> {
                bleStateImageSrc.set(R.drawable.ic_bluetooth_connected)
                bleStateImageTintColor.set(R.color.cc_gn_800)
            }
            is MainActivityState.BleDisconnected -> {
                bleStateImageSrc.set(R.drawable.ic_bluetooth_disconnected)
                bleStateImageTintColor.set(R.color.cc_rd_800)
            }
            is MainActivityState.Error -> {
                state.message?.let { showNotification(it) }
            }
        }
    }

    private fun showNotification(message: String) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_top, 0, 0, R.anim.fade_out)
                .replace(R.id.frame_container, NotificationFragment.getFragment(message), NotificationFragment::class.java.simpleName)
                .addToBackStack(null)
                .commit()
    }

    private fun hideNotification() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frame_container)
        if (fragment != null && fragment is NotificationFragment) {
            supportFragmentManager.popBackStack()
        }
    }

    private fun initViewPager() {
        val fragments = arrayOf<Fragment>(
                ActualFragment.getFragment(),
                HistoryFragment.getFragment(),
                SettingsFragment.getFragment()
        )

        navigationPagerAdapter = NavigationPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = navigationPagerAdapter
        view_pager.offscreenPageLimit = fragments.size
    }

    private fun initBottomNavigation() {
        bottom_navigation.setOnNavigationItemSelectedListener { menuItem ->
            hideNotification()

            when (menuItem.itemId) {
                R.id.action_actual -> view_pager.setCurrentItem(0, true)
                R.id.action_history -> view_pager.setCurrentItem(1, true)
                R.id.action_settings -> view_pager.setCurrentItem(2, true)
            }
            true
        }
    }

    private fun checkPermissions(requestCode: Int, vararg permissions: String): Boolean {
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permissions_rationale), requestCode, *permissions)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        when (requestCode) {
            RC_PERMISSIONS -> {
                Timber.plant(FileLoggingTree(this))
            }
        }
    }

}
