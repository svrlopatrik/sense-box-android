package sk.kotlin.sensebox.ui.activities.detail

import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.activity_detail.*
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.bl.vm.DetailActivityViewModel
import sk.kotlin.sensebox.models.states.DetailActivityState
import sk.kotlin.sensebox.ui.activities.BaseActivity
import sk.kotlin.sensebox.ui.activities.main.NavigationPagerAdapter
import sk.kotlin.sensebox.ui.fragments.detail_chart.DetailChartFragment
import sk.kotlin.sensebox.ui.fragments.detail_list.DetailListFragment

class DetailActivity : BaseActivity<DetailActivityViewModel>(DetailActivityViewModel::class.java) {

    companion object {
        private const val KEY_FILE_ID = "key_file_id"
        private const val KEY_FILE = "key_file"

        fun startActivity(context: Context, file: File) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(KEY_FILE_ID, file.id)
            intent.putExtra(KEY_FILE, file)
            context.startActivity(intent)
        }
    }

    private lateinit var navigationPagerAdapter: NavigationPagerAdapter
    private lateinit var file: File

    override fun setLayout() = R.layout.activity_detail

    override fun initViews(savedInstanceState: Bundle?) {
        if (!intent.hasExtra(KEY_FILE) || !intent.hasExtra(KEY_FILE_ID)) {
            return
        }
        file = intent.getParcelableExtra(KEY_FILE)
        file.id = intent.getStringExtra(KEY_FILE_ID)

        viewModel?.loadRecords(file)

        observeState()
        initViewPager()
        initTabLayout()
        initBackButton()
    }

    private fun observeState() {
        viewModel?.getDetailActivityState()?.observe(this, Observer { state -> state?.let { render(it) } })
    }

    private fun render(state: DetailActivityState) {
        when (state) {
            is DetailActivityState.Success -> {
            }
            is DetailActivityState.Error -> {
            }
        }
    }

    private fun initViewPager() {
        val fragments = arrayOf<Fragment>(
                DetailChartFragment.getFragment(),
                DetailListFragment.getFragment()
        )

        navigationPagerAdapter = NavigationPagerAdapter(supportFragmentManager, fragments)
        view_pager.adapter = navigationPagerAdapter
        view_pager.offscreenPageLimit = fragments.size
    }

    private fun initTabLayout() {
        layout_tab.apply {
            setupWithViewPager(view_pager)
            getTabAt(0)?.setText(R.string.chart)
            getTabAt(1)?.setText(R.string.list)
        }
    }

    private fun initBackButton() {
        button_back.setOnClickListener {
            finish()
            overridePendingTransition(0, R.anim.slide_out_top)
        }
    }

    override fun onBackPressed() {
        if (view_pager.currentItem != 0) {
            view_pager.currentItem = 0
        } else {
            super.onBackPressed()
            overridePendingTransition(0, R.anim.slide_out_top)
        }
    }

}