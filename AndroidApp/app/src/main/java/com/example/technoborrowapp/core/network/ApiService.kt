package com.example.technoborrowapp.core.network

import com.example.technoborrowapp.features.auth.data.model.LoginRequest
import com.example.technoborrowapp.features.auth.data.model.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("auth/register")
    fun register(@Body user: User): Call<User>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ResponseBody>

    @GET("auth/profile/{id}")
    fun getProfile(@Path("id") id: Long): Call<User>

    @PUT("auth/profile/edit/{id}")
    fun updateProfile(@Path("id") id: Long, @Body user: User): Call<ResponseBody>

    @PUT("auth/profile/password/{id}")
    fun changePassword(@Path("id") id: Long, @Body body: Map<String, String>): Call<ResponseBody>

    @Multipart
    @POST("auth/profile/upload/{id}")
    fun uploadPhoto(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>

    @GET("borrowing-requests")
    fun getAllRequests(): Call<List<com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest>>

    @POST("borrowing-requests")
    fun createRequest(@Body request: com.example.technoborrowapp.features.dashboard.data.model.CreateBorrowingRequestDTO): Call<com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest>

    @POST("offers")
    fun createOffer(@Body offer: com.example.technoborrowapp.features.dashboard.data.model.CreateOfferDTO): Call<com.example.technoborrowapp.features.dashboard.data.model.Offer>

    @GET("offers/request/{requestId}")
    fun getOffersForRequest(@Path("requestId") requestId: Long): Call<List<com.example.technoborrowapp.features.dashboard.data.model.Offer>>

    @GET("offers/user/{userId}")
    fun getOffersForUser(@Path("userId") userId: Long): Call<List<com.example.technoborrowapp.features.dashboard.data.model.Offer>>


    @POST("offers/{offerId}/accept")
    fun acceptOffer(@Path("offerId") offerId: Long): Call<ResponseBody>

    @POST("borrowing-requests/{id}/borrow")
    fun confirmBorrow(@Path("id") id: Long): Call<com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest>

    @POST("borrowing-requests/{id}/return")
    fun confirmReturn(@Path("id") id: Long, @Query("userId") userId: Long): Call<com.example.technoborrowapp.features.dashboard.data.model.BorrowingRequest>
}

