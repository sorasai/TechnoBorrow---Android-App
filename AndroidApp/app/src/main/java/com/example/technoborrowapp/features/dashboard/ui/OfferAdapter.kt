package com.example.technoborrowapp.features.dashboard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.technoborrowapp.R
import com.example.technoborrowapp.features.dashboard.data.model.Offer
import com.google.android.material.button.MaterialButton

class OfferAdapter(
    private var offers: List<Offer>,
    private val onAcceptClick: (Offer) -> Unit
) : RecyclerView.Adapter<OfferAdapter.OfferViewHolder>() {

    fun updateData(newOffers: List<Offer>) {
        offers = newOffers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_offer, parent, false)
        return OfferViewHolder(view)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(offers[position])
    }

    override fun getItemCount(): Int = offers.size

    inner class OfferViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvOfferLenderName)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvOfferMessage)
        private val btnAccept: MaterialButton = itemView.findViewById(R.id.btnAcceptOffer)
        private val tvAccepted: TextView = itemView.findViewById(R.id.tvAcceptedLabel)

        fun bind(offer: Offer) {
            tvName.text = offer.lenderName ?: "Lender #${offer.lenderId}"
            tvMessage.text = offer.message ?: "I can lend this item."

            if (offer.status == "ACCEPTED") {
                btnAccept.visibility = View.GONE
                tvAccepted.visibility = View.VISIBLE
            } else {
                btnAccept.visibility = View.VISIBLE
                tvAccepted.visibility = View.GONE
                btnAccept.setOnClickListener { onAcceptClick(offer) }
            }
        }
    }
}
