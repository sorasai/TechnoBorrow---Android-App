package com.example.technoborrowapp.features.dashboard.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest
import com.example.technoborrowapp.features.dashboard.data.model.CreateBorrowingRequestDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*

class CreateRequestActivity : AppCompatActivity() {

    private lateinit var etItemName: EditText
    private lateinit var etDescription: EditText
    private lateinit var etPurpose: EditText
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var tvDuration: TextView
    private lateinit var ivPreview: ImageView
    
    private var base64Image: String? = null
    private var startDateTime: Calendar? = null
    private var endDateTime: Calendar? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            ivPreview.setImageBitmap(bitmap)
            ivPreview.setPadding(0, 0, 0, 0)
            
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_request)

        setupUI()
    }

    private fun setupUI() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        etItemName = findViewById(R.id.etItemName)
        etDescription = findViewById(R.id.etDescription)
        etPurpose = findViewById(R.id.etPurpose)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        tvDuration = findViewById(R.id.tvDuration)
        ivPreview = findViewById(R.id.ivItemPreview)
        val btnSubmit = findViewById<Button>(R.id.btnSubmitRequest)

        toolbar.setNavigationOnClickListener { finish() }

        tvStartDate.setOnClickListener { showDateTimePicker { cal -> 
            startDateTime = cal
            tvStartDate.text = formatDateTime(cal)
            updateDuration()
        }}

        tvEndDate.setOnClickListener { showDateTimePicker { cal -> 
            endDateTime = cal
            tvEndDate.text = formatDateTime(cal)
            updateDuration()
        }}

        ivPreview.setOnClickListener { pickImage.launch("image/*") }

        btnSubmit.setOnClickListener { submitRequest() }
    }

    private fun updateDuration() {
        val start = startDateTime
        val end = endDateTime
        
        if (start != null && end != null) {
            val diffMs = end.timeInMillis - start.timeInMillis
            if (diffMs < 0) {
                tvDuration.text = "Error: End must be after Start"
                tvDuration.setTextColor(android.graphics.Color.RED)
            } else {
                val diffHrs = diffMs.toDouble() / (1000 * 60 * 60)
                tvDuration.text = String.format("Total Duration: %.1f hours", diffHrs)
                if (diffHrs > 24) {
                    tvDuration.setTextColor(android.graphics.Color.RED)
                    Toast.makeText(this, "Duration cannot exceed 24 hours", Toast.LENGTH_SHORT).show()
                } else {
                    tvDuration.setTextColor(android.graphics.Color.parseColor("#7A1E2D"))
                }
            }
        }
    }

    private fun showDateTimePicker(onSelected: (Calendar) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val selected = Calendar.getInstance()
                selected.set(year, month, day, hour, minute)
                onSelected(selected)
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDateTime(cal: Calendar): String {
        return String.format("%04d-%02d-%02d %02d:%02d", 
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

    private fun submitRequest() {
        val name = etItemName.text.toString().trim()
        val desc = etDescription.text.toString().trim()
        val purpose = etPurpose.text.toString().trim()

        if (name.isEmpty() || desc.isEmpty() || purpose.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val start = startDateTime
        val end = endDateTime

        if (start == null || end == null) {
            Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show()
            return
        }

        val diffHrs = (end.timeInMillis - start.timeInMillis).toDouble() / (1000 * 60 * 60)
        if (diffHrs < 0 || diffHrs > 24) {
            Toast.makeText(this, "Invalid duration (must be 0-24 hours)", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        val userId = sharedPref.getLong("user_id", -1L)

        if (userId == -1L) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        // yyyy-MM-ddTHH:mm:ss
        val startStr = String.format("%04d-%02d-%02dT%02d:%02d:00",
            start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1, start.get(Calendar.DAY_OF_MONTH),
            start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE))
        
        val endStr = String.format("%04d-%02d-%02dT%02d:%02d:00",
            end.get(Calendar.YEAR), end.get(Calendar.MONTH) + 1, end.get(Calendar.DAY_OF_MONTH),
            end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE))

        val dto = CreateBorrowingRequestDTO(
            requesterId = userId,
            itemName = name,
            description = desc,
            purpose = purpose,
            startDate = startStr,
            endDate = endStr,
            itemImage = base64Image
        )

        RetrofitClient.instance.createRequest(dto)
            .enqueue(object : Callback<BorrowingRequest> {
                override fun onResponse(call: Call<BorrowingRequest>, response: Response<BorrowingRequest>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateRequestActivity, "Request posted successfully!", Toast.LENGTH_LONG).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@CreateRequestActivity, "Failed to post request", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<BorrowingRequest>, t: Throwable) {
                    Toast.makeText(this@CreateRequestActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
