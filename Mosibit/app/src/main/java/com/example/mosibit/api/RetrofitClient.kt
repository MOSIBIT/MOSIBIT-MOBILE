package com.example.mosibit.api


import com.example.mosibit.data.Data
import com.example.mosibit.data.ItemResponse
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