package com.example.campusmarket.data.model

data class StoreResponse(
    val code: String,
    val message: String,
    val result: StoreResult,
    val success: Boolean
)

data class StoreResult(
    val stores: List<Store>,
    val page: Int,
    val size: Int,
    val hasNext: Boolean
)



data class Store(
    val sellerId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val latestProductImageUrl: String?,
    val saleCount: Int,
    val purchaseCount: Int
)