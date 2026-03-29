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
    val latestProductImageUrl: String?,   // 상세 페이지용 실제 사진
    val displayAssetImageUrl: String?,    // 목록 페이지용 아이콘 경로
    val saleCount: Int,
    val purchaseCount: Int
)