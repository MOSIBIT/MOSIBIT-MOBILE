package com.example.mediapipemultihandstrackingapp.ui.translate

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mediapipemultihandstrackingapp.api.Config
import com.example.mediapipemultihandstrackingapp.data.Data
import com.example.mediapipemultihandstrackingapp.data.ItemResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TranslateViewModel : ViewModel() {
    val testing = MutableLiveData<ItemResponse>()
    fun post(data: Data) {
        val config = Config.create().cordinate(data)
        config.enqueue(
            object : Callback<ItemResponse>{
                override fun onResponse(
                    call: Call<ItemResponse>,
                    response: Response<ItemResponse>
                ) {
                    if (response.isSuccessful){
                        testing.value = response.body()
                        Log.d("PREDICTION", response.body()?.alphabet.toString())
                    }
                }

                override fun onFailure(call: Call<ItemResponse>, t: Throwable) {
                   Log.d("TESTING", t.message.toString())
                }

            }
        )
    }
}