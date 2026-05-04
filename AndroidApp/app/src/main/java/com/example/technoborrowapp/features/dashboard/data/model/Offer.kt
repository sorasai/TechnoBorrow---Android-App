package com.example.technoborrowapp.features.dashboard.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Offer(
    val id: Long,
    val requestId: Long,
    val lenderId: Long,
    val lenderName: String?,
    val message: String?,
    val status: String,
    val createdAt: String?
) : Parcelable

@Parcelize
data class CreateOfferDTO(
    val requestId: Long,
    val lenderId: Long,
    val message: String
) : Parcelable
