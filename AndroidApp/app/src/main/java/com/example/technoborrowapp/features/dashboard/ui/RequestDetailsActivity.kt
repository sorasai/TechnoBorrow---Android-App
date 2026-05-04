package com.example.technoborrowapp.features.dashboard.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest
import com.example.technoborrowapp.features.dashboard.data.model.CreateOfferDTO
import com.example.technoborrowapp.features.dashboard.data.model.Offer
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RequestDetailsActivity : AppCompatActivity() {

    private lateinit var offerAdapter: OfferAdapter
    private var currentUserId: Long = -1L
    private lateinit var currentRequest: BorrowingRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_details)

        val req = intent.getParcelableExtra<BorrowingRequest>("request")
        if (req == null) {
            Toast.makeText(this, "Error loading request details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentRequest = req

        val sharedPref = getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getLong("user_id", -1L)

        setupUI()
        updateFlow()
    }


    private fun setupUI() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val ivItem = findViewById<ImageView>(R.id.ivItemImageDet)
        val tvName = findViewById<TextView>(R.id.tvItemNameDet)
        val tvStatus = findViewById<TextView>(R.id.tvStatusDet)
        val tvDesc = findViewById<TextView>(R.id.tvDescriptionDet)
        val tvPurpose = findViewById<TextView>(R.id.tvPurposeDet)
        val tvStart = findViewById<TextView>(R.id.tvScheduleStartDet)
        val tvEnd = findViewById<TextView>(R.id.tvScheduleEndDet)
        val ivRequester = findViewById<ShapeableImageView>(R.id.ivRequesterAvatarDet)
        val tvRequesterName = findViewById<TextView>(R.id.tvRequesterNameDet)

        toolbar.setNavigationOnClickListener { finish() }

        tvName.text = currentRequest.itemName
        tvStatus.text = currentRequest.status
        tvDesc.text = currentRequest.description
        tvPurpose.text = currentRequest.purpose ?: "No purpose provided"
        tvStart.text = currentRequest.startDate?.replace("T", " ")?.substring(0, 16)
        tvEnd.text = currentRequest.endDate?.replace("T", " ")?.substring(0, 16)
        tvRequesterName.text = currentRequest.requesterName

        currentRequest.itemImage?.let {
            try {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ivItem.setImageBitmap(bitmap)
                ivItem.clearColorFilter()
            } catch (e: Exception) {}
        }

        currentRequest.requesterImage?.let {
            try {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ivRequester.setImageBitmap(bitmap)
            } catch (e: Exception) {}
        }

        val rvOffers = findViewById<RecyclerView>(R.id.rvOffers)
        rvOffers.layoutManager = LinearLayoutManager(this)
        offerAdapter = OfferAdapter(emptyList()) { offer ->
            acceptOffer(offer.id)
        }
        rvOffers.adapter = offerAdapter

        val btnOfferToLend = findViewById<MaterialButton>(R.id.btnOfferToLend)
        val btnConfirmBorrow = findViewById<MaterialButton>(R.id.btnConfirmBorrow)
        val btnConfirmReturn = findViewById<MaterialButton>(R.id.btnConfirmReturn)

        btnOfferToLend.setOnClickListener {
            createOffer()
        }

        btnConfirmBorrow.setOnClickListener {
            confirmBorrow()
        }

        btnConfirmReturn.setOnClickListener {
            confirmReturn()
        }
    }

    private fun updateFlow() {
        val tvStatus = findViewById<TextView>(R.id.tvStatusDet)
        val cvOffersSection = findViewById<CardView>(R.id.cvOffersSection)
        val btnOfferToLend = findViewById<MaterialButton>(R.id.btnOfferToLend)
        val btnConfirmBorrow = findViewById<MaterialButton>(R.id.btnConfirmBorrow)
        val btnConfirmReturn = findViewById<MaterialButton>(R.id.btnConfirmReturn)
        val tvReturnStatus = findViewById<TextView>(R.id.tvReturnStatus)
        val btnContact = findViewById<MaterialButton>(R.id.btnContactLender)

        cvOffersSection.visibility = View.GONE
        btnOfferToLend.visibility = View.GONE
        btnConfirmBorrow.visibility = View.GONE
        btnConfirmReturn.visibility = View.GONE
        tvReturnStatus.visibility = View.GONE
        btnContact.visibility = View.GONE

        val step1Circle = findViewById<TextView>(R.id.step1_circle)
        val step1Label = findViewById<TextView>(R.id.step1_label)
        val step2Circle = findViewById<TextView>(R.id.step2_circle)
        val step2Label = findViewById<TextView>(R.id.step2_label)
        val step3Circle = findViewById<TextView>(R.id.step3_circle)
        val step3Label = findViewById<TextView>(R.id.step3_label)
        val step4Circle = findViewById<TextView>(R.id.step4_circle)
        val step4Label = findViewById<TextView>(R.id.step4_label)

        val currentStep = when (currentRequest.status.uppercase()) {
            "POSTED" -> 0
            "MATCHED", "ONGOING" -> 1
            "BORROWED", "BORROWER_RETURNED", "LENDER_RETURNED" -> 2
            "RETURNED", "COMPLETED" -> 3
            else -> 0
        }

        val circles = arrayOf(step1Circle, step2Circle, step3Circle, step4Circle)
        val labels = arrayOf(step1Label, step2Label, step3Label, step4Label)

        for (i in 0..3) {
            if (i < currentStep) {
                circles[i].setBackgroundResource(R.drawable.stepper_circle_completed)
                circles[i].setTextColor(android.graphics.Color.WHITE)
                circles[i].text = "✓"
                labels[i].setTextColor(android.graphics.Color.parseColor("#10B981"))
            } else if (i == currentStep) {
                circles[i].setBackgroundResource(R.drawable.stepper_circle_active)
                circles[i].setTextColor(android.graphics.Color.WHITE)
                circles[i].text = (i + 1).toString()
                labels[i].setTextColor(android.graphics.Color.parseColor("#7A1E2D"))
            } else {
                circles[i].setBackgroundResource(R.drawable.stepper_circle_inactive)
                circles[i].setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
                circles[i].text = (i + 1).toString()
                labels[i].setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
            }
        }

        tvStatus.text = currentRequest.status
        val isOwnRequest = currentUserId == currentRequest.requesterId

        when (currentRequest.status.uppercase()) {

            "POSTED" -> {
                if (isOwnRequest) {
                    cvOffersSection.visibility = View.VISIBLE
                    fetchOffersForRequest()
                } else {
                    btnOfferToLend.visibility = View.VISIBLE
                    checkIfAlreadyOffered(btnOfferToLend)
                }
            }
            "MATCHED", "ONGOING" -> {
                if (isOwnRequest) {
                    btnConfirmBorrow.visibility = View.VISIBLE
                }
            }
            "BORROWED", "BORROWER_RETURNED", "LENDER_RETURNED" -> {
                fetchOffersAndHandleReturn(btnConfirmReturn, tvReturnStatus)
            }
        }
    }

    private fun fetchOffersForRequest() {
        val tvNoOffersYet = findViewById<TextView>(R.id.tvNoOffersYet)
        val rvOffers = findViewById<RecyclerView>(R.id.rvOffers)
        
        RetrofitClient.instance.getOffersForRequest(currentRequest.id)
            .enqueue(object : Callback<List<Offer>> {
                override fun onResponse(call: Call<List<Offer>>, response: Response<List<Offer>>) {
                    if (response.isSuccessful) {
                        val offers = response.body() ?: emptyList()
                        offerAdapter.updateData(offers)
                        
                        if (offers.isEmpty()) {
                            tvNoOffersYet.visibility = View.VISIBLE
                            rvOffers.visibility = View.GONE
                        } else {
                            tvNoOffersYet.visibility = View.GONE
                            rvOffers.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onFailure(call: Call<List<Offer>>, t: Throwable) {
                    Toast.makeText(this@RequestDetailsActivity, "Error fetching offers", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun checkIfAlreadyOffered(btn: MaterialButton) {
        RetrofitClient.instance.getOffersForRequest(currentRequest.id)
            .enqueue(object : Callback<List<Offer>> {
                override fun onResponse(call: Call<List<Offer>>, response: Response<List<Offer>>) {
                    if (response.isSuccessful) {
                        val offers = response.body() ?: emptyList()
                        val hasOffered = offers.any { it.lenderId == currentUserId }
                        if (hasOffered) {
                            btn.isEnabled = false
                            btn.text = "Offer Sent"
                            btn.setBackgroundColor(android.graphics.Color.parseColor("#E2E8F0"))
                            btn.setTextColor(android.graphics.Color.parseColor("#94A3B8"))
                        }
                    }
                }
                override fun onFailure(call: Call<List<Offer>>, t: Throwable) {}
            })
    }

    private fun fetchOffersAndHandleReturn(btn: MaterialButton, tvStatusText: TextView) {
        RetrofitClient.instance.getOffersForRequest(currentRequest.id)
            .enqueue(object : Callback<List<Offer>> {
                override fun onResponse(call: Call<List<Offer>>, response: Response<List<Offer>>) {
                    if (response.isSuccessful) {
                        val offers = response.body() ?: emptyList()
                        val acceptedOffer = offers.find { it.status.uppercase() == "ACCEPTED" }
                        if (acceptedOffer != null) {
                            val isLender = currentUserId == acceptedOffer.lenderId
                            val isBorrower = currentUserId == currentRequest.requesterId

                            when (currentRequest.status.uppercase()) {
                                "BORROWED" -> {
                                    if (isBorrower) {
                                        btn.visibility = View.VISIBLE
                                        btn.text = "Mark as Returned"
                                    } else if (isLender) {
                                        btn.visibility = View.VISIBLE
                                        btn.text = "Confirm Return"
                                    }
                                }
                                "BORROWER_RETURNED" -> {
                                    if (isBorrower) {
                                        tvStatusText.visibility = View.VISIBLE
                                        tvStatusText.text = "Waiting for other user to confirm return..."
                                    } else if (isLender) {
                                        btn.visibility = View.VISIBLE
                                        btn.text = "Confirm Return"
                                    }
                                }
                                "LENDER_RETURNED" -> {
                                    if (isBorrower) {
                                        btn.visibility = View.VISIBLE
                                        btn.text = "Mark as Returned"
                                    } else if (isLender) {
                                        tvStatusText.visibility = View.VISIBLE
                                        tvStatusText.text = "Waiting for other user to confirm return..."
                                    }
                                }
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<List<Offer>>, t: Throwable) {}
            })
    }

    private fun createOffer() {
        val dto = CreateOfferDTO(currentRequest.id, currentUserId, "I can lend this item.")
        RetrofitClient.instance.createOffer(dto)
            .enqueue(object : Callback<Offer> {
                override fun onResponse(call: Call<Offer>, response: Response<Offer>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RequestDetailsActivity, "Offer submitted!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this@RequestDetailsActivity, "Failed to submit offer", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Offer>, t: Throwable) {
                    Toast.makeText(this@RequestDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun acceptOffer(offerId: Long) {
        RetrofitClient.instance.acceptOffer(offerId)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RequestDetailsActivity, "Offer accepted!", Toast.LENGTH_SHORT).show()
                        currentRequest = currentRequest.copy(status = "MATCHED")
                        updateFlow()
                        setResult(RESULT_OK)
                    } else {
                        Toast.makeText(this@RequestDetailsActivity, "Failed to accept offer", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@RequestDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmBorrow() {
        RetrofitClient.instance.confirmBorrow(currentRequest.id)
            .enqueue(object : Callback<BorrowingRequest> {
                override fun onResponse(call: Call<BorrowingRequest>, response: Response<BorrowingRequest>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@RequestDetailsActivity, "Confirmed borrow receipt!", Toast.LENGTH_SHORT).show()
                        currentRequest = response.body()!!
                        updateFlow()
                        setResult(RESULT_OK)
                    } else {
                        Toast.makeText(this@RequestDetailsActivity, "Failed to confirm borrow", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<BorrowingRequest>, t: Throwable) {
                    Toast.makeText(this@RequestDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmReturn() {
        RetrofitClient.instance.confirmReturn(currentRequest.id, currentUserId)
            .enqueue(object : Callback<BorrowingRequest> {
                override fun onResponse(call: Call<BorrowingRequest>, response: Response<BorrowingRequest>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@RequestDetailsActivity, "Return confirmed!", Toast.LENGTH_SHORT).show()
                        currentRequest = response.body()!!
                        updateFlow()
                        setResult(RESULT_OK)
                    } else {
                        Toast.makeText(this@RequestDetailsActivity, "Failed to confirm return", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<BorrowingRequest>, t: Throwable) {
                    Toast.makeText(this@RequestDetailsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
