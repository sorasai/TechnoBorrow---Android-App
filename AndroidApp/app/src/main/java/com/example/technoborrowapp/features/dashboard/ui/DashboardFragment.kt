package com.example.technoborrowapp.features.dashboard.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.auth.data.model.User
import com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardFragment : Fragment() {

    private lateinit var adapter: RequestAdapter
    private lateinit var rvRequests: RecyclerView
    private lateinit var tvEmptyRequests: TextView
    private lateinit var tvStatActive: TextView
    private lateinit var tvStatOngoing: TextView
    private lateinit var tvStatReturned: TextView
    
    private var currentUserId: Long = -1L

    private val createRequestLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            fetchData() 
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        fetchData()
    }

    private fun setupUI(view: View) {
        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcome)
        rvRequests = view.findViewById(R.id.rvRequests)
        tvEmptyRequests = view.findViewById(R.id.tvEmptyRequests)
        
        tvStatActive = view.findViewById(R.id.tvStatActiveRequests)
        tvStatOngoing = view.findViewById(R.id.tvStatOngoingTransactions)
        tvStatReturned = view.findViewById(R.id.tvStatReturnedTransactions)

        val cardActive = view.findViewById<View>(R.id.cardActiveRequests)
        val cardOngoing = view.findViewById<View>(R.id.cardOngoingTransactions)
        val cardReturned = view.findViewById<View>(R.id.cardReturnedTransactions)

        val sharedPref = requireActivity().getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        val fullName = sharedPref.getString("full_name", "User")
        currentUserId = sharedPref.getLong("user_id", -1L)

        tvWelcome.text = "Welcome, ${fullName?.split(" ")?.get(0)}!"

        val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)

        cardActive.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_requests
        }
        cardOngoing.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_transactions
        }
        cardReturned.setOnClickListener {
            bottomNav.selectedItemId = R.id.nav_transactions
        }

        val btnCreate = view.findViewById<android.widget.Button>(R.id.btnDashboardCreateRequest)
        btnCreate.setOnClickListener {
            val intent = Intent(requireContext(), CreateRequestActivity::class.java)
            createRequestLauncher.launch(intent)
        }

        rvRequests.layoutManager = LinearLayoutManager(requireContext())

        adapter = RequestAdapter(emptyList()) { request ->
            val intent = Intent(requireContext(), RequestDetailsActivity::class.java)
            intent.putExtra("request", request)
            startActivity(intent)
        }
        rvRequests.adapter = adapter
    }

    private fun fetchData() {
        RetrofitClient.instance.getAllRequests().enqueue(object : Callback<List<BorrowingRequest>> {
            override fun onResponse(call: Call<List<BorrowingRequest>>, response: Response<List<BorrowingRequest>>) {
                if (isAdded && response.isSuccessful) {
                    val all = response.body() ?: emptyList()
                    
                    // Active Requests Stat: User's requests where status is not Returned or Cancelled
                    val activeCount = all.filter { it.requesterId == currentUserId && it.status.uppercase() != "RETURNED" && it.status.uppercase() != "CANCELLED" }.size
                    tvStatActive.text = activeCount.toString()

                    // The explorer list shows all requests that are posted/pending, sorted so user's own requests are at the top
                    val explorer = all.filter { it.status.uppercase() == "POSTED" || it.status.uppercase() == "PENDING" }
                        .sortedWith(compareByDescending<BorrowingRequest> { it.requesterId == currentUserId }
                            .thenByDescending { it.id })
                    adapter.updateData(explorer)


                    if (explorer.isEmpty()) {
                        tvEmptyRequests.visibility = View.VISIBLE
                        rvRequests.visibility = View.GONE
                    } else {
                        tvEmptyRequests.visibility = View.GONE
                        rvRequests.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<List<BorrowingRequest>>, t: Throwable) {
                if (isAdded) Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        if (currentUserId != -1L) {
            RetrofitClient.instance.getOffersForUser(currentUserId).enqueue(object : Callback<List<com.example.technoborrowapp.features.dashboard.data.model.Offer>> {
                override fun onResponse(
                    call: Call<List<com.example.technoborrowapp.features.dashboard.data.model.Offer>>, 
                    response: Response<List<com.example.technoborrowapp.features.dashboard.data.model.Offer>>
                ) {
                    if (isAdded && response.isSuccessful) {
                        val offers = response.body() ?: emptyList()
                        val offeredReqIds = offers.map { it.requestId }.toSet()

                        // To populate stats, fetch requests user participated in
                        RetrofitClient.instance.getAllRequests().enqueue(object : Callback<List<BorrowingRequest>> {
                            override fun onResponse(call: Call<List<BorrowingRequest>>, res: Response<List<BorrowingRequest>>) {
                                if (isAdded && res.isSuccessful) {
                                    val allReq = res.body() ?: emptyList()
                                    val myTrans = allReq.filter { offeredReqIds.contains(it.id) }

                                    val ongoingCount = myTrans.filter { 
                                        val status = it.status.uppercase()
                                        status == "MATCHED" || status == "BORROWED" || status == "BORROWER_RETURNED" || status == "LENDER_RETURNED" || status == "ONGOING"
                                    }.size
                                    
                                    val returnedCount = myTrans.filter { it.status.uppercase() == "RETURNED" }.size
                                    
                                    tvStatOngoing.text = ongoingCount.toString()
                                    tvStatReturned.text = returnedCount.toString()
                                }
                            }
                            override fun onFailure(call: Call<List<BorrowingRequest>>, t: Throwable) {}
                        })
                    }
                }
                override fun onFailure(call: Call<List<com.example.technoborrowapp.features.dashboard.data.model.Offer>>, t: Throwable) {}
            })
        }
    }

}
