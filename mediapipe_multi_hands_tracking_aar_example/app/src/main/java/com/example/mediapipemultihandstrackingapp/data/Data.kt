package com.example.mediapipemultihandstrackingapp.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Data(
    //for collecting hand coordinate data from camera
    @SerializedName("data")
    @Expose
    var data: List<List<List<Double>>>
)