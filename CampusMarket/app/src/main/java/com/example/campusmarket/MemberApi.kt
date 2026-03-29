package com.example.campusmarket.network

import com.example.campusmarket.model.NicknameCheckResponse
import com.example.campusmarket.model.RandomNicknameResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MemberApi {

    @GET("members/random-nickname")
    suspend fun getRandomNickname(): Response<RandomNicknameResponse>

    @GET("members/nickname/check")
    suspend fun checkNickname(
        @Query("nickname") nickname: String
    ): Response<NicknameCheckResponse>
}