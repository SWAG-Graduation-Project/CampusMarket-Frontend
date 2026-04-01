package com.example.campusmarket.data.model

data class MyStoreResponse(
    val code: String,
    val message: String,
    val result: MyStoreResult? = null,
    val data: MyStoreResult? = null,
    val success: Boolean? = null
)

data class MyStoreResult(
    val memberId: Long,
    val nickname: String?,
    val profileImageUrl: String?,
    val saleCount: Int?,
    val purchaseCount: Int?,
    val latestProducts: List<MyStoreLatestProduct> = emptyList()
)

data class MyStoreLatestProduct(
    val productId: Long,
    val productName: String,
    val price: Int,
    val thumbnailImageUrl: String?
)