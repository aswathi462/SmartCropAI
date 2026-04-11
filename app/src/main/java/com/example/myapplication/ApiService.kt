package com.example.myapplication

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("predict")
    fun predictYield(
        @Body input: CropInput
    ): Call<CropResponse>
}