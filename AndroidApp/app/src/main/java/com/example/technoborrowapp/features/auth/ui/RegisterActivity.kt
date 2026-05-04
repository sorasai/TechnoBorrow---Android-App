package com.example.technoborrowapp.features.auth.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.auth.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        tvLogin?.setOnClickListener {
            finish() // Back to Login
        }

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(fullName = fullName, email = email, passwordHash = password)

            RetrofitClient.instance.register(user)
                .enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@RegisterActivity, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                            Toast.makeText(this@RegisterActivity, "Registration failed: $errorMsg", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
