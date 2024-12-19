package com.irwan.aplikasistoryapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @POST("register")
    suspend fun registerUser(
        @Body register: Register
    ): ResponseRegister

    @POST("login")
    suspend fun loginUser(
        @Body loginRq: LoginRq
    ): LoginResponse


    @Multipart
    @POST("stories")
    fun addNewStory(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): Response<ResponseRegister>
}