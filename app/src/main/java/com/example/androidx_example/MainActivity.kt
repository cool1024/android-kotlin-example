package com.example.androidx_example

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupWithNavController
import com.example.androidx_example.until.debugInfo
import com.example.androidx_example.until.showToast
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

class MainActivity : BaseActivity() {

    private var canBack = false
    private var mExitClickTime: Long = 0
    private val navCtrl: NavController by lazy {
        nav_host.findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onBackPressedDispatcher.addCallback {
            isEnabled = false
            remove()
            debugInfo("按下返回")
        }
        initNav()
    }

    override fun onBackPressed() {
        if (canBack) {
            super.onBackPressed()
        } else {
            if (System.currentTimeMillis() - mExitClickTime > 2000) {
                showToast("再按一次退出程序", this)
                mExitClickTime = System.currentTimeMillis()
            } else {
                exitProcess(0)
            }
        }
    }

    private fun initNav() {
        val navCtrl = nav_host.findNavController().apply {
            addOnDestinationChangedListener { _, destination, _ ->

                canBack = destination.id !in listOf(
                    R.id.homeFragment,
                    R.id.webFragment,
                    R.id.publicFragment,
                    R.id.userCenterFragment
                )

                main_bottom_navigation?.visibility = if (canBack) View.GONE else View.VISIBLE
                navigation_side?.setCheckedItem(destination.id)
            }
        }
        main_bottom_navigation.setOnNavigationItemSelectedListener { item ->

            onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host))
        }
//        main_bottom_navigation.setupWithNavController(navCtrl)
//        navigation_side.apply {
//            setupWithNavController(navCtrl)
//            itemIconTintList = null
//        }
    }

}
