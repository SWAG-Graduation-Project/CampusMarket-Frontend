package com.example.campusmarket.network

import com.example.campusmarket.data.model.CommonResponse
import com.example.campusmarket.data.model.MyProfileResponse
import com.example.campusmarket.data.model.TimetableClassRequest
import com.example.campusmarket.data.model.TimetableClassResponse
import com.example.campusmarket.data.model.TimetableParseResponse
import com.example.campusmarket.data.model.TimetableResponse
import com.example.campusmarket.data.model.TimetableUpdateResponse
import com.example.campusmarket.model.NicknameCheckResponse
import com.example.campusmarket.model.ProfileInitRequest
import com.example.campusmarket.model.ProfileInitResponse
import com.example.campusmarket.model.RandomNicknameResponse
import com.example.campusmarket.network.dto.LockerGetResponse
import com.example.campusmarket.network.dto.LockerSaveRequest
import com.example.campusmarket.network.dto.LockerSaveResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface MemberApi {

    @GET("members/random-nickname")
    suspend fun getRandomNickname(): Response<RandomNicknameResponse>

    @GET("members/nickname/check")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<NicknameCheckResponse>

    // ─── 사물함 ───────────────────────────────────────────────

    @PATCH("members/locker")
    suspend fun saveMyLocker(
        @Header("guestUuid") guestUuid: String,
        @Body request: LockerSaveRequest
    ): Response<LockerSaveResponse>

    @GET("members/locker")
    suspend fun getMyLocker(
        @Header("guestUuid") guestUuid: String
    ): Response<LockerGetResponse>

    @DELETE("members/locker")
    suspend fun deleteMyLocker(
        @Header("guestUuid") guestUuid: String
    ): Response<CommonResponse>

    // ─── 프로필 ───────────────────────────────────────────────

    @POST("members/profile")
    suspend fun saveProfile(
        @Header("guestUuid") guestUuid: String,
        @Body request: ProfileInitRequest
    ): Response<ProfileInitResponse>

    @PATCH("members/profile")
    suspend fun updateProfile(
        @Header("guestUuid") guestUuid: String,
        @Body request: ProfileInitRequest
    ): Response<ProfileInitResponse>

    @GET("members/profile")
    suspend fun getMyProfile(
        @Header("guestUuid") guestUuid: String
    ): Response<MyProfileResponse>

    // ─── 시간표 ───────────────────────────────────────────────

    @Multipart
    @POST("/members/timetable")
    suspend fun uploadTimetableImage(
        @Header("guestUuid") guestUuid: String,
        @Part file: MultipartBody.Part
    ): Response<TimetableResponse>

    @GET("members/timetable")
    suspend fun getMyTimetable(
        @Header("guestUuid") guestUuid: String
    ): Response<TimetableResponse>

    @Multipart
    @POST("members/timetable/parse")
    suspend fun parseTimetableImage(
        @Header("guestUuid") guestUuid: String,
        @Part file: MultipartBody.Part
    ): Response<TimetableParseResponse>

    @GET("members/timetable/classes/{classIndex}")
    suspend fun getTimetableClass(
        @Header("guestUuid") guestUuid: String,
        @Path("classIndex") classIndex: Int
    ): Response<TimetableClassResponse>

    @PATCH("members/timetable/classes/{classIndex}")
    suspend fun updateTimetableClass(
        @Header("guestUuid") guestUuid: String,
        @Path("classIndex") classIndex: Int,
        @Body request: TimetableClassRequest
    ): Response<TimetableUpdateResponse>

    @DELETE("members/timetable/classes/{classIndex}")
    suspend fun deleteTimetableClass(
        @Header("guestUuid") guestUuid: String,
        @Path("classIndex") classIndex: Int
    ): Response<TimetableUpdateResponse>

    // ─── 회원 탈퇴 ────────────────────────────────────────────

    @DELETE("members")
    suspend fun withdrawMember(
        @Header("guestUuid") guestUuid: String
    ): Response<CommonResponse>
}
