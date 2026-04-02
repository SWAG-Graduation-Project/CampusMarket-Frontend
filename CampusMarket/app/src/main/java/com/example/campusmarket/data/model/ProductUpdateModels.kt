package com.example.campusmarket.data.model

data class ProductUpdateRequest(
    val name: String?,
    val brand: String?,
    val color: String?,
    val productCondition: String?,
    val description: String?,
    val price: Int?,
    val isFree: Boolean?
)

data class ProfileImageUploadResult(
    val imageUrl: String
)

data class ProfileImageUploadResponse(
    val code: String,
    val message: String,
    val result: ProfileImageUploadResult?,
    val success: Boolean
)
