package com.example.campusmarket.data.model

import com.google.gson.annotations.SerializedName

data class TimetableClassRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("day") val day: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("location") val location: String?
)

data class TimetableClassResult(
    @SerializedName("name") val name: String?,
    @SerializedName("day") val day: String?,
    @SerializedName("start_time") val startTime: String?,
    @SerializedName("end_time") val endTime: String?,
    @SerializedName("location") val location: String?
)

data class TimetableClassResponse(
    val code: String,
    val message: String,
    val result: TimetableClassResult?,
    val success: Boolean
)

data class TimetableUpdateResult(
    val timetableData: String
)

data class TimetableUpdateResponse(
    val code: String,
    val message: String,
    val result: TimetableUpdateResult?,
    val success: Boolean
)
