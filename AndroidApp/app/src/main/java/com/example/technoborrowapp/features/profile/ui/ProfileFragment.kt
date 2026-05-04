package com.example.technoborrowapp.features.profile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.auth.data.model.User
import com.example.technoborrowapp.features.auth.ui.LoginActivity
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etNewPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var ivAvatar: ImageView
    private var userId: Long = -1L

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            uploadSelectedPhoto(it)
        }
    }

    private fun uploadSelectedPhoto(uri: android.net.Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes != null) {
                val requestFile = RequestBody.create(
                    MediaType.parse(requireContext().contentResolver.getType(uri) ?: "image/jpeg"),
                    bytes
                )
                val body = MultipartBody.Part.createFormData("file", "avatar.jpg", requestFile)

                RetrofitClient.instance.uploadPhoto(userId, body).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (isAdded && response.isSuccessful) {
                            Toast.makeText(requireContext(), "Photo uploaded successfully", Toast.LENGTH_SHORT).show()
                            loadData()
                        } else {
                            Toast.makeText(requireContext(), "Failed to upload photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        if (isAdded) Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        loadData()
    }

    private fun setupUI(view: View) {
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.navigationIcon = null
        
        etName = view.findViewById(R.id.etProfileName)
        etEmail = view.findViewById(R.id.etProfileEmail)
        etNewPass = view.findViewById(R.id.etNewPassword)
        etConfirmPass = view.findViewById(R.id.etConfirmNewPassword)
        ivAvatar = view.findViewById(R.id.ivBigAvatar)

        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)
        val btnUpdatePass = view.findViewById<Button>(R.id.btnUpdatePassword)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnUpload = view.findViewById<TextView>(R.id.btnUploadPhoto)

        btnSave.setOnClickListener { updateProfile() }
        btnUpdatePass.setOnClickListener { changePassword() }
        btnLogout.setOnClickListener { logout() }
        
        btnUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val sharedPref = requireActivity().getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        userId = sharedPref.getLong("user_id", -1L)
    }


    private fun loadData() {
        if (userId == -1L) return

        RetrofitClient.instance.getProfile(userId)
            .enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (isAdded && response.isSuccessful) {
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
                override fun onFailure(call: Call<User>, t: Throwable) {}
            })
    }

    private fun updateProfile() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) return

        val userUpdate = User(fullName = name, email = etEmail.text.toString())
        RetrofitClient.instance.updateProfile(userId, userUpdate)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (isAdded && response.isSuccessful) {
                        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                        val sharedPref = requireActivity().getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("full_name", name).apply()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
    }

    private fun changePassword() {
        val newPass = etNewPass.text.toString()
        val confirmPass = etConfirmPass.text.toString()

        if (newPass.isEmpty() || newPass != confirmPass) {
            Toast.makeText(requireContext(), "Check your passwords", Toast.LENGTH_SHORT).show()
            return
        }

        val body = mapOf("newPassword" to newPass)
        RetrofitClient.instance.changePassword(userId, body)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (isAdded && response.isSuccessful) {
                        Toast.makeText(requireContext(), "Password changed", Toast.LENGTH_SHORT).show()
                        etNewPass.setText("")
                        etConfirmPass.setText("")
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
            })
    }

    private fun logout() {
        val sharedPref = requireActivity().getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
