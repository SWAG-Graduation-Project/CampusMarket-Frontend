package com.example.campusmarket.data.model

data class MemberProfileResponse(
    val code: String,
    val message: String,
    val data: MemberProfileData?
)

data class MemberProfileData(
    val memberId: Long?,
    val guestUuid: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val lockerName: String?,
    val timetableImageUrl: String?,
    val profileCompleted: Boolean?
)