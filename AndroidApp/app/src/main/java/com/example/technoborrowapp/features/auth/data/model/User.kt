package com.example.technoborrowapp.features.auth.data.model

data class User(
    val id: Long? = null,
    val email: String,
    val fullName: String,
    val passwordHash: String? = null,
    val profileImage: String? = null
)
