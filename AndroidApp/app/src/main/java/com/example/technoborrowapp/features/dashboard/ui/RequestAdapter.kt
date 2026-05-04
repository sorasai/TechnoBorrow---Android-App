package com.example.technoborrowapp.features.dashboard.ui

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.technoborrowapp.R
import com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest

class RequestAdapter(
    private var requests: List<BorrowingRequest>,
    private val onItemClick: (BorrowingRequest) -> Unit
) : RecyclerView.Adapter<RequestAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvRequesterName: TextView = view.findViewById(R.id.tvRequesterName)
        val tvCreatedAt: TextView = view.findViewById(R.id.tvCreatedAt)
        val ivRequesterAvatar: ImageView = view.findViewById(R.id.ivRequesterAvatar)
        val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.tvItemName.text = request.itemName
        holder.tvStatus.text = request.status
        holder.tvDescription.text = request.description
        holder.tvRequesterName.text = request.requesterName
        
        // Simple date formatting (just show the raw string for now or take first 10 chars)
        holder.tvCreatedAt.text = request.createdAt.take(10)

        // Set avatar if exists
        request.requesterImage?.let {
            try {
                val imageBytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.ivRequesterAvatar.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.ivRequesterAvatar.setImageResource(R.mipmap.ic_launcher)
            }
        }

        holder.btnViewDetails.setOnClickListener { onItemClick(request) }
    }

    override fun getItemCount() = requests.size

    fun updateData(newRequests: List<BorrowingRequest>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
