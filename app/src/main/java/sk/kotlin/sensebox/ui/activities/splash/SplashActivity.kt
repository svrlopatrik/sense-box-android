package sk.kotlin.sensebox.ui.activities.splash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.ui.activities.main.MainActivity

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        MainActivity.startActivity(this)
        overridePendingTransition(R.anim.fade_out, 0)
        finish()
        super.onCreate(savedInstanceState)
    }
}