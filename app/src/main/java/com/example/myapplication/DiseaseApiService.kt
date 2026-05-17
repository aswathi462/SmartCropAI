package com.example.myapplication

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DiseaseApiService {

    @Multipart
    @POST("upload_leaf")
    fun uploadLeaf(
        @Part image: MultipartBody.Part
    ): Call<DiseaseResponse>
}