package com.example.campusmarket.network

import com.example.campusmarket.model.NicknameCheckResponse
import com.example.campusmarket.model.ProfileInitRequest
import com.example.campusmarket.model.ProfileInitResponse
import com.example.campusmarket.model.RandomNicknameResponse
import com.example.campusmarket.model.TimetableParseResponse
import com.example.campusmarket.network.dto.LockerGetResponse
import com.example.campusmarket.network.dto.LockerSaveRequest
import com.example.campusmarket.network.dto.LockerSaveResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


interface MemberApi {

    @GET("members/random-nickname")
    suspend fun getRandomNickname(): Response<RandomNicknameResponse>

    @GET("members/nickname/check")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<NicknameCheckResponse>

    @PATCH("members/locker")
    suspend fun saveMyLocker(
        @Header("guestUuid") guestUuid: String,
        @Body request: LockerSaveRequest
    ): Response<LockerSaveResponse>

    @GET("members/locker")
    suspend fun getMyLocker(
        @Header("guestUuid") guestUuid: String
    ): Response<LockerGetResponse>
    @POST("members/profile")
    suspend fun saveProfile(
        @Header("guestUuid") guestUuid: String,
        @Header("memberId") memberId: Long,
        @Body request: ProfileInitRequest
    ): Response<ProfileInitResponse>

    @Multipart
    @POST("members/timetable/parse")
    suspend fun parseTimetableImage(
        @Header("guestUuid") guestUuid: String,
        @Part file: MultipartBody.Part
    ): Response<TimetableParseResponse>

}