package com.example.habittrackerapplication

import StaticsFragment
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.habittrackerapplication.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNav: BottomNavigationView = binding.bottomNav

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    setPageTitle(getString(R.string.home_title))
                    showUserName(true)
                }
                R.id.nav_statics -> {
                    loadFragment(StaticsFragment())
                    setPageTitle(getString(R.string.statics_title))
                    showUserName(false)
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    setPageTitle(getString(R.string.settings_title))
                    showUserName(false)
                }
                else -> return@setOnItemSelectedListener false
            }
            true
        }

        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun setPageTitle(title: String) {
        binding.toolbarPageTitle.text = title
    }

    private fun showUserName(show: Boolean) {
        binding.userNameText.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun updateUserName(name: String) {
        binding.userNameText.text = name
    }
}
