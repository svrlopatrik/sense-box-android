package sk.kotlin.sensebox.ui.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.MainActivityViewModel
import sk.kotlin.sensebox.ui.activities.BaseActivity

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class MainActivity : BaseActivity<MainActivityViewModel>(MainActivityViewModel::class.java) {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }

    override fun setLayout() = R.layout.activity_main

    override fun initViews(savedInstanceState: Bundle?) {

    }

}
