package com.example.technoborrowapp.features.dashboard.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.technoborrowapp.R
import com.example.technoborrowapp.core.network.RetrofitClient
import com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest
import com.example.technoborrowapp.features.dashboard.data.model.Offer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyTransactionsFragment : Fragment() {

    private lateinit var ongoingAdapter: RequestAdapter
    private lateinit var pastAdapter: RequestAdapter
    private lateinit var rvOngoing: RecyclerView
    private lateinit var rvPast: RecyclerView
    private lateinit var tvEmptyOngoing: android.widget.TextView
    private lateinit var tvEmptyPast: android.widget.TextView
    private var currentUserId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_transactions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getLong("user_id", -1L)

        setupUI(view)
        fetchData()
    }

    private fun setupUI(view: View) {
        rvOngoing = view.findViewById(R.id.rvOngoingTransactions)
        rvPast = view.findViewById(R.id.rvTransactionHistory)
        tvEmptyOngoing = view.findViewById(R.id.tvEmptyOngoingTransactions)
        tvEmptyPast = view.findViewById(R.id.tvEmptyTransactionHistory)

        rvOngoing.layoutManager = LinearLayoutManager(requireContext())
        rvPast.layoutManager = LinearLayoutManager(requireContext())

        ongoingAdapter = RequestAdapter(emptyList()) { request ->
            openDetails(request)
        }

        pastAdapter = RequestAdapter(emptyList()) { request ->
            openDetails(request)
        }

        rvOngoing.adapter = ongoingAdapter
        rvPast.adapter = pastAdapter
    }

    private fun openDetails(request: BorrowingRequest) {
        val intent = Intent(requireContext(), RequestDetailsActivity::class.java)
        intent.putExtra("request", request)
        startActivity(intent)
    }

    private fun fetchData() {
        RetrofitClient.instance.getOffersForUser(currentUserId).enqueue(object : Callback<List<Offer>> {
            override fun onResponse(call: Call<List<Offer>>, response: Response<List<Offer>>) {
                if (isAdded && response.isSuccessful) {
                    val offers = response.body() ?: emptyList()
                    val offeredReqIds = offers.map { it.requestId }.toSet()
                    
                    fetchRequestsAndFilter(offeredReqIds)
                }
            }

            override fun onFailure(call: Call<List<Offer>>, t: Throwable) {
                if (isAdded) Toast.makeText(requireContext(), "Failed to fetch offers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchRequestsAndFilter(offeredReqIds: Set<Long>) {
        RetrofitClient.instance.getAllRequests().enqueue(object : Callback<List<BorrowingRequest>> {
            override fun onResponse(call: Call<List<BorrowingRequest>>, response: Response<List<BorrowingRequest>>) {
                if (isAdded && response.isSuccessful) {
                    val all = response.body() ?: emptyList()
                    val myTransactions = all.filter { offeredReqIds.contains(it.id) || it.requesterId == currentUserId }
                    
                    val ongoing = myTransactions.filter { 
                        val status = it.status.uppercase()
                        status == "MATCHED" || status == "BORROWED" || status == "BORROWER_RETURNED" || status == "LENDER_RETURNED" || status == "ONGOING"
                    }
                    val past = myTransactions.filter { it.status.uppercase() == "RETURNED" || it.status.uppercase() == "CANCELLED" }

                    
                    ongoingAdapter.updateData(ongoing)
                    pastAdapter.updateData(past)

                    if (ongoing.isEmpty()) {
                        tvEmptyOngoing.visibility = View.VISIBLE
                        rvOngoing.visibility = View.GONE
                    } else {
                        tvEmptyOngoing.visibility = View.GONE
                        rvOngoing.visibility = View.VISIBLE
                    }

                    if (past.isEmpty()) {
                        tvEmptyPast.visibility = View.VISIBLE
                        rvPast.visibility = View.GONE
                    } else {
                        tvEmptyPast.visibility = View.GONE
                        rvPast.visibility = View.VISIBLE
                    }
                }
            }

            override fun onFailure(call: Call<List<BorrowingRequest>>, t: Throwable) {
                if (isAdded) Toast.makeText(requireContext(), "Failed to fetch requests", Toast.LENGTH_SHORT).show()
            }
        })
    }
}


