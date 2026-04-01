package com.example.campusmarket

import com.example.campusmarket.network.ApiService
import com.example.campusmarket.network.AuthApi
import com.example.campusmarket.network.MemberApi
import com.example.campusmarket.network.ProductImageApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://3.36.120.78:8080/api/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val memberApi: MemberApi by lazy {
        retrofit.create(MemberApi::class.java)
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val productImageApi: ProductImageApi by lazy {
        retrofit.create(ProductImageApi::class.java)
    }
}