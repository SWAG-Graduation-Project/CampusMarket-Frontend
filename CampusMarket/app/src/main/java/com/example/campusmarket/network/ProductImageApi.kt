package com.example.campusmarket.network

import com.example.campusmarket.data.model.BackgroundRemovalRequest
import com.example.campusmarket.data.model.BackgroundRemovalResponse
import com.example.campusmarket.data.model.TempImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ProductImageApi {

    @Multipart
    @POST("products/images/temp")
    suspend fun uploadTempImage(
        @Header("X-Guest-UUID") guestUuid: String?,
        @Header("X-Member-Id") memberId: Long?,
        @Part files: MultipartBody.Part
    ): Response<TempImageUploadResponse>

    @POST("products/images/background-removal")
    suspend fun removeBackground(
        @Header("X-Guest-UUID") guestUuid: String?,
        @Header("X-Member-Id") memberId: Long?,
        @Body request: BackgroundRemovalRequest
    ): Response<BackgroundRemovalResponse>
}