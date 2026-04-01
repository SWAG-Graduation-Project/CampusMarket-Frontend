package com.example.campusmarket.data.model

data class ProductListResponse(
    val code: String,
    val message: String,
    val result: ProductListResult,
    val success: Boolean
)

data class ProductListResult(
    val products: List<ProductItem>?,
    val pageInfo: PageInfo?
)

data class ProductItem(
    val productId: Long,
    val name: String?,
    val brand: String?,
    val price: Int?,
    val isFree: Boolean?,
    val productCondition: String?,
    val saleStatus: String?,
    val viewCount: Int?,
    val wishCount: Int?,
    val displayAssetImageUrl: String?,
    val thumbnailImageUrl: String?,
    val createdAt: String?,
    val sellerId: Long?,
    val sellerNickname: String?
)

data class PageInfo(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean
)