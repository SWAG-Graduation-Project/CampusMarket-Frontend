package com.example.campusmarket.data.model

data class WishlistToggleResult(
    val productId: Long,
    val wished: Boolean,
    val wishCount: Int
)

data class WishlistToggleResponse(
    val code: String,
    val message: String,
    val result: WishlistToggleResult?,
    val success: Boolean
)

data class WishlistProduct(
    val productId: Long,
    val productName: String?,
    val price: Int?,
    val saleStatus: String?,
    val thumbnailImageUrl: String?,
    val wishCount: Int?,
    val createdAt: String?
)

data class WishlistProductsResult(
    val products: List<WishlistProduct>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
)

data class WishlistProductsResponse(
    val code: String,
    val message: String,
    val result: WishlistProductsResult?,
    val success: Boolean
)
