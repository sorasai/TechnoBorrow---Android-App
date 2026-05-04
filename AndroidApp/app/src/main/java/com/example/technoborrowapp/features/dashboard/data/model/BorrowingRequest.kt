package com.example.technoborrowapp.features.dashboard.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BorrowingRequest(
    val id: Long,
    val itemName: String,
    val description: String,
    val purpose: String?,
    val startDate: String?,
    val endDate: String?,
    val itemImage: String?,
    val requesterName: String,
    val requesterImage: String?,
    val createdAt: String,
    val status: String,
    val requesterId: Long? = null,
    val offerCount: Int? = 0
) : Parcelable

