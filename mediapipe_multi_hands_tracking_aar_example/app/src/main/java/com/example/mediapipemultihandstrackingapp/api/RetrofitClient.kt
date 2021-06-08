package com.example.mediapipemultihandstrackingapp.api


import com.example.mediapipemultihandstrackingapp.data.Data
import com.example.mediapipemultihandstrackingapp.data.ItemResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

@JvmSuppressWildcards
interface RetrofitClient {

    @POST("predict")
    fun cordinate(
        @Body data: Data
    ): Call<ItemResponse>
}