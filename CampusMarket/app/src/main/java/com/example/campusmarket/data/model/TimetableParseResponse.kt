package com.example.campusmarket.model

data class TimetableParseResponse(
    val code: String,
    val message: String,
    val result: TimetableParseResult?,
    val success: Boolean
)

data class TimetableParseResult(
    val timetableData: String
)