package com.example.campusmarket

import androidx.annotation.DrawableRes

data class LockerGroupData(
    val buildingName: String,
    val floor: Int,
    val imageIndex: Int,
    val originalX: Float,
    val originalY: Float,
    val major: String,
    val groupNumber: Int,
    @DrawableRes val lockerImageResId: Int,
    val offsetX: Float = 40f,
    val offsetY: Float = 120f
)

data class LoungeImageData(
    val buildingName: String,
    val floor: Int,
    val imageIndex: Int,
    @DrawableRes val imageResId: Int
)

data class SelectedLockerGroup(
    val buildingName: String,
    val floor: Int,
    val imageIndex: Int,
    val major: String,
    val groupNumber: Int
)