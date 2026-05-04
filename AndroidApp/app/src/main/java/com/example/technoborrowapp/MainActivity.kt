package com.example.technoborrowapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.technoborrowapp.features.dashboard.ui.DashboardFragment
import com.example.technoborrowapp.features.dashboard.ui.MyRequestsFragment
import com.example.technoborrowapp.features.dashboard.ui.MyTransactionsFragment

import com.example.technoborrowapp.features.dashboard.ui.PlaceholderFragment
import com.example.technoborrowapp.features.profile.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_requests -> {
                    loadFragment(MyRequestsFragment())
                    true
                }
                R.id.nav_transactions -> {
                    loadFragment(MyTransactionsFragment())
                    true
                }

                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

        // Load default fragment
        if (savedInstanceState == null) {
            navView.selectedItemId = R.id.nav_dashboard
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
