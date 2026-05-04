package com.example.technoborrowapp.features.dashboard.data.model

data class CreateBorrowingRequestDTO(
    val requesterId: Long,
    val itemName: String,
    val description: String,
    val purpose: String,
    val startDate: String,
    val endDate: String,
    val itemImage: String? = null
)
