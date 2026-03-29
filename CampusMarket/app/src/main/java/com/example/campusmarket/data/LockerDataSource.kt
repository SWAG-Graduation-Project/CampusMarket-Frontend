package com.example.campusmarket.data

import com.example.campusmarket.LockerGroupData
import com.example.campusmarket.LoungeImageData
import com.example.campusmarket.R

object LockerDataSource {

    val loungeImageList = listOf(
        LoungeImageData("차관", 1, 1, R.drawable.cha_center1),
        LoungeImageData("차관", 2, 1, R.drawable.cha_center2),
        LoungeImageData("차관", 3, 1, R.drawable.cha_center3),
        LoungeImageData("차관", 4, 1, R.drawable.cha_center4)
    )

    val lockerList = listOf(
        LockerGroupData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 637f,
            originalY = 915f,
            major = "경영",
            groupNumber = 1,
            lockerImageResId = R.drawable.lockerback1,

        ),
        LockerGroupData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 795f,
            originalY = 907f,
            major = "경영",
            groupNumber = 2,
            lockerImageResId = R.drawable.lockerback2,

        )
    )
}