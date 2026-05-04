package com.example.technoborrowapp.features.profile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.auth.data.model.User
import com.example.technoborrowapp.features.auth.ui.LoginActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etNewPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var ivAvatar: ImageView
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupUI()
        loadData()
    }

    private fun setupUI() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        etName = findViewById(R.id.etProfileName)
        etEmail = findViewById(R.id.etProfileEmail)
        etNewPass = findViewById(R.id.etNewPassword)
        etConfirmPass = findViewById(R.id.etConfirmNewPassword)
        ivAvatar = findViewById(R.id.ivBigAvatar)

        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        val btnUpdatePass = findViewById<Button>(R.id.btnUpdatePassword)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnUpload = findViewById<TextView>(R.id.btnUploadPhoto)

        toolbar.setNavigationOnClickListener { finish() }

        btnSave.setOnClickListener { updateProfile() }
        btnUpdatePass.setOnClickListener { changePassword() }
        btnLogout.setOnClickListener { logout() }
        
        btnUpload.setOnClickListener {
            Toast.makeText(this, "Photo upload coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Get saved user ID
        val sharedPref = getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        userId = sharedPref.getLong("user_id", -1L)
    }

    private fun loadData() {
        if (userId == -1L) {
            Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
            logout()
            return
        }

        RetrofitClient.instance.getProfile(userId)
            .enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            etName.setText(user.fullName)
                            etEmail.setText(user.email)
                            
                            user.profileImage?.let { imgStr ->
                                try {
                                    val decodedString = android.util.Base64.decode(imgStr, android.util.Base64.DEFAULT)
                                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                                    ivAvatar.setImageBitmap(bitmap)
                                } catch (e: Exception) {}
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateProfile() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) return

        val userUpdate = User(fullName = name, email = etEmail.text.toString())
        RetrofitClient.instance.updateProfile(userId, userUpdate)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                        // Update local shared pref
                        val sharedPref = getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("full_name", name).apply()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun changePassword() {
        val newPass = etNewPass.text.toString()
        val confirmPass = etConfirmPass.text.toString()

        if (newPass.isEmpty()) {
            Toast.makeText(this, "Please enter new password", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPass != confirmPass) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mapOf("newPassword" to newPass)
        RetrofitClient.instance.changePassword(userId, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileActivity, "Password changed successfully", Toast.LENGTH_SHORT).show()
                        etNewPass.setText("")
                        etConfirmPass.setText("")
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to change password", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun logout() {
        val sharedPref = getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
