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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyRequestsFragment : Fragment() {

    private lateinit var activeAdapter: RequestAdapter
    private lateinit var pastAdapter: RequestAdapter
    private lateinit var rvActive: RecyclerView
    private lateinit var rvPast: RecyclerView
    private lateinit var tvEmptyActive: android.widget.TextView
    private lateinit var tvEmptyPast: android.widget.TextView
    private var currentUserId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val sharedPref = requireActivity().getSharedPreferences("technoborrow", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getLong("user_id", -1L)

        setupUI(view)
        fetchData()
    }

    private fun setupUI(view: View) {
        rvActive = view.findViewById(R.id.rvActiveRequests)
        rvPast = view.findViewById(R.id.rvPastRequests)
        tvEmptyActive = view.findViewById(R.id.tvEmptyActiveRequests)
        tvEmptyPast = view.findViewById(R.id.tvEmptyPastRequests)

        rvActive.layoutManager = LinearLayoutManager(requireContext())
        rvPast.layoutManager = LinearLayoutManager(requireContext())

        activeAdapter = RequestAdapter(emptyList()) { request ->
            openDetails(request)
        }

        pastAdapter = RequestAdapter(emptyList()) { request ->
            openDetails(request)
        }

        rvActive.adapter = activeAdapter
        rvPast.adapter = pastAdapter
    }

    private fun openDetails(request: BorrowingRequest) {
        val intent = Intent(requireContext(), RequestDetailsActivity::class.java)
        intent.putExtra("request", request)
        startActivity(intent)
    }

    private fun fetchData() {
        RetrofitClient.instance.getAllRequests().enqueue(object : Callback<List<BorrowingRequest>> {
            override fun onResponse(call: Call<List<BorrowingRequest>>, response: Response<List<BorrowingRequest>>) {
                if (isAdded && response.isSuccessful) {
                    val all = response.body() ?: emptyList()
                    val myRequests = all.filter { it.requesterId == currentUserId }
                    
                    val active = myRequests.filter { it.status.uppercase() != "RETURNED" && it.status.uppercase() != "CANCELLED" }
                    val past = myRequests.filter { it.status.uppercase() == "RETURNED" || it.status.uppercase() == "CANCELLED" }
                    
                    activeAdapter.updateData(active)
                    pastAdapter.updateData(past)

                    if (active.isEmpty()) {
                        tvEmptyActive.visibility = View.VISIBLE
                        rvActive.visibility = View.GONE
                    } else {
                        tvEmptyActive.visibility = View.GONE
                        rvActive.visibility = View.VISIBLE
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
