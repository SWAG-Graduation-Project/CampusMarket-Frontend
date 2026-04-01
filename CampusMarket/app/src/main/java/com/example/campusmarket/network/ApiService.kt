package com.example.campusmarket.network

import com.example.campusmarket.data.model.ChatRoomRequest
import com.example.campusmarket.data.model.ChatRoomResponse
import com.example.campusmarket.data.model.MajorCategoryResponse
import com.example.campusmarket.data.model.ProductDetailResponse
import com.example.campusmarket.data.model.StoreResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("categories/major")
    suspend fun getMajorCategories(): MajorCategoryResponse

    @GET("stores")
    suspend fun getStores(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12
    ): StoreResponse

    @POST("chat/rooms")
    suspend fun createOrEnterChatRoom(
        @Body request: ChatRoomRequest
    ): ChatRoomResponse
    @GET("/api/products/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: Long,
        @Header("X-Member-Id") memberId: Long
    ): ProductDetailResponse

}