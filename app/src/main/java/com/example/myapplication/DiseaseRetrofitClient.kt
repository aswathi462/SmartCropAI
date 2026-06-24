package com.example.myapplication

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DiseaseRetrofitClient {

    private const val BASE_URL = "http://10.247.186.253:8001/"

    val api: DiseaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DiseaseApiService::class.java)
    }
}