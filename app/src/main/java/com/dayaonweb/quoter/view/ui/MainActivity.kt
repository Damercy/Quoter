package com.dayaonweb.quoter.view.ui

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.dayaonweb.quoter.R
import com.dayaonweb.quoter.databinding.ActivityMainBinding

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var actViewModel: MainActivityViewModel
    private lateinit var actViewModelFactory: MainActivityViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actViewModelFactory = MainActivityViewModelFactory(this)
        actViewModel =
            ViewModelProvider(this, actViewModelFactory).get(MainActivityViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        actViewModel.initUser(this)
        setupBottomNavBar()
    }

    private fun setupBottomNavBar() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)
        binding.bottomNav.setOnNavigationItemReselectedListener { {} }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (isNightTheme == Configuration.UI_MODE_NIGHT_YES) {
            menuInflater.inflate(R.menu.action_bar_items, menu)
            val item = menu?.findItem(R.id.themeSwitch) ?: return true
            actViewModel.tintMenuIcon(this, item, R.color.white)
        } else {
            menuInflater.inflate(R.menu.action_bar_items_dark, menu)
            val item = menu?.findItem(R.id.themeSwitch) ?: return true
            actViewModel.tintMenuIcon(this, item, R.color.white)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.themeSwitch -> {
                actViewModel.themeOnClickListener(this)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}