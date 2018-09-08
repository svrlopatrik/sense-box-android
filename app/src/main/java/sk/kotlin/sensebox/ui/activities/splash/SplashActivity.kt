package sk.kotlin.sensebox.ui.activities.splash

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import sk.kotlin.sensebox.ui.activities.main.MainActivity

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        MainActivity.startActivity(this)
        finish()
        super.onCreate(savedInstanceState)
    }
}