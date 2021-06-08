package com.example.mediapipemultihandstrackingapp.data

import com.google.gson.annotations.SerializedName

data class ItemResponse(
    //for collecting response from api
    @SerializedName("prediction")
    var alphabet: String
)