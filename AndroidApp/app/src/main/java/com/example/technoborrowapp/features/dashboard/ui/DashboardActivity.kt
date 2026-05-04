package com.example.technoborrowapp.features.dashboard.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.auth.data.model.User
import com.example.technoborrowapp.features.profile.ui.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardActivity : AppCompatActivity() {

    private val createRequestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            if (currentFragment is DashboardFragment) {
                loadFragment(DashboardFragment())
            } else if (currentFragment is MyRequestsFragment) {
                loadFragment(MyRequestsFragment())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        setupUI()
        loadFragment(DashboardFragment())
    }

    private fun setupUI() {
        val ivAvatar = findViewById<ImageView>(R.id.ivToolbarAvatar)
        val fabCreate = findViewById<FloatingActionButton>(R.id.fabCreateRequest)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val sharedPref = getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        val userId = sharedPref.getLong("user_id", -1L)

        if (userId != -1L) {
            RetrofitClient.instance.getProfile(userId).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        response.body()?.profileImage?.let { imgStr ->
                            try {
                                val decodedString = android.util.Base64.decode(imgStr, android.util.Base64.DEFAULT)
                                val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                ivAvatar.setImageBitmap(bitmap)
                            } catch (e: Exception) {}
                        }
                    }
                }
                override fun onFailure(call: Call<User>, t: Throwable) {}
            })
        }

        ivAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        fabCreate.setOnClickListener {
            createRequestLauncher.launch(Intent(this, CreateRequestActivity::class.java))
        }

        bottomNav.setOnItemSelectedListener { item ->
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment)
        transaction.commit()
    }
}

