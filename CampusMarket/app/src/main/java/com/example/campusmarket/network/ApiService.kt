package com.example.campusmarket.network

import com.example.campusmarket.data.model.ChatImageUploadResponse
import com.example.campusmarket.data.model.ChatMessagesResponse
import com.example.campusmarket.data.model.ChatRoomRequest
import com.example.campusmarket.data.model.ChatRoomResponse
import com.example.campusmarket.data.model.CommonResponse
import com.example.campusmarket.data.model.MajorCategoryResponse
import com.example.campusmarket.data.model.MyStoreResponse
import com.example.campusmarket.data.model.ProductDetailResponse
import com.example.campusmarket.data.model.ProductListResponse
import com.example.campusmarket.data.model.ProductUpdateRequest
import com.example.campusmarket.data.model.ProfileImageUploadResponse
import com.example.campusmarket.data.model.ProposalRequest
import com.example.campusmarket.data.model.ProposalRespondRequest
import com.example.campusmarket.data.model.ProposalResponse
import com.example.campusmarket.data.model.SellingChatRoomsResponse
import com.example.campusmarket.data.model.StoreResponse
import com.example.campusmarket.data.model.UserMarketProductsResponse
import com.example.campusmarket.data.model.UserStoreDetailResponse
import com.example.campusmarket.data.model.WishlistProductsResponse
import com.example.campusmarket.data.model.WishlistToggleResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("categories/major")
    suspend fun getMajorCategories(): MajorCategoryResponse

    @GET("stores")
    suspend fun getStores(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoreResponse

    // ─── 채팅방 ───────────────────────────────────────────────

    @POST("chat/rooms")
    suspend fun createOrEnterChatRoom(
        @Header("guestUuid") guestUuid: String,
        @Body request: ChatRoomRequest
    ): Response<ChatRoomResponse>

    @GET("chat/rooms/selling")
    suspend fun getSellingChatRooms(
        @Header("guestUuid") guestUuid: String
    ): Response<SellingChatRoomsResponse>

    @GET("chat/rooms/buying")
    suspend fun getBuyingChatRooms(
        @Header("guestUuid") guestUuid: String
    ): Response<SellingChatRoomsResponse>

    @GET("chat/rooms/{chatRoomId}/messages")
    suspend fun getChatMessages(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<ChatMessagesResponse>

    @DELETE("chat/rooms/{chatRoomId}")
    suspend fun leaveChatRoom(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long
    ): Response<CommonResponse>

    @Multipart
    @POST("chat/rooms/{chatRoomId}/images")
    suspend fun uploadChatImage(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long,
        @Part file: MultipartBody.Part
    ): Response<ChatImageUploadResponse>

    // ─── 거래 제안 ────────────────────────────────────────────

    @POST("chat/rooms/{chatRoomId}/proposals")
    suspend fun createProposal(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long,
        @Body request: ProposalRequest
    ): Response<ProposalResponse>

    @PATCH("chat/rooms/{chatRoomId}/proposals/{proposalId}")
    suspend fun respondToProposal(
        @Header("guestUuid") guestUuid: String,
        @Path("chatRoomId") chatRoomId: Long,
        @Path("proposalId") proposalId: Long,
        @Body request: ProposalRespondRequest
    ): Response<ProposalResponse>

    // ─── 상품 ─────────────────────────────────────────────────

    @GET("products/{productId}")
    suspend fun getProductDetail(
        @Path("productId") productId: Long
    ): ProductDetailResponse

    @POST("products/{productId}/views")
    suspend fun recordProductView(
        @Path("productId") productId: Long
    ): Response<CommonResponse>

    @GET("/api/products")
    suspend fun getProducts(
        @Query("majorCategoryId") majorCategoryId: Long? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 12,
        @Query("sort") sort: String? = null
    ): ProductListResponse

    @GET("my-store/products/{productId}")
    suspend fun getMyProductDetail(
        @Header("X-Guest-UUID") guestUuid: String,
        @Path("productId") productId: Long
    ): Response<ProductDetailResponse>

    @PATCH("my-store/products/{productId}")
    suspend fun updateProduct(
        @Header("X-Guest-UUID") guestUuid: String,
        @Path("productId") productId: Long,
        @Body request: ProductUpdateRequest
    ): Response<CommonResponse>

    @PATCH("my-store/products/{productId}/sold")
    suspend fun markProductSold(
        @Header("X-Guest-UUID") guestUuid: String,
        @Path("productId") productId: Long
    ): Response<CommonResponse>

    // ─── 상점 ─────────────────────────────────────────────────

    @GET("stores/{sellerId}")
    suspend fun getStoreDetail(
        @Path("sellerId") sellerId: Long
    ): UserStoreDetailResponse

    @GET("stores/{sellerId}/products")
    suspend fun getStoreProducts(
        @Path("sellerId") sellerId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): UserMarketProductsResponse

    @GET("my-store")
    suspend fun getMyStore(
        @Header("X-Guest-UUID") guestUuid: String
    ): Response<MyStoreResponse>

    // ─── 찜 ───────────────────────────────────────────────────

    @POST("products/{productId}/wishlist")
    suspend fun toggleWishlist(
        @Header("X-Guest-UUID") guestUuid: String,
        @Path("productId") productId: Long
    ): Response<WishlistToggleResponse>

    @GET("wishlist/products")
    suspend fun getWishlistProducts(
        @Header("X-Guest-UUID") guestUuid: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<WishlistProductsResponse>

    // ─── 파일 업로드 ──────────────────────────────────────────

    @Multipart
    @POST("files/upload/profile-image")
    suspend fun uploadProfileImage(
        @Header("guestUuid") guestUuid: String,
        @Part file: MultipartBody.Part
    ): Response<ProfileImageUploadResponse>
}
