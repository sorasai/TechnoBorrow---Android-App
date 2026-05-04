package com.example.technoborrowapp.features.auth.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.technoborrowapp.MainActivity
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.auth.data.model.LoginRequest
import com.example.technoborrowapp.features.auth.data.model.User
import com.example.technoborrowapp.features.dashboard.ui.DashboardActivity
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        tvRegister?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)

            RetrofitClient.instance.login(request)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        val responseBody = response.body()?.string() ?: response.errorBody()?.string()
                        
                        if (response.isSuccessful && responseBody != null) {
                            try {
                                if (responseBody.trim().startsWith("{")) {
                                    // It's likely a JSON object (User)
                                    val user = Gson().fromJson(responseBody, User::class.java)
                                    if (user != null && user.id != null) {
                                        saveSessionAndRedirect(user)
                                    } else {
                                        Toast.makeText(this@LoginActivity, "Server error: Invalid user data", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    // It's likely a plain error message string
                                    Toast.makeText(this@LoginActivity, responseBody, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Login failed: ${responseBody ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun saveSessionAndRedirect(user: User) {
        val sharedPref = getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("user_id", user.id ?: -1L)
            putString("user_email", user.email)
            putString("full_name", user.fullName)
            apply()
        }

        Toast.makeText(this, "Welcome, ${user.fullName}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
