package com.example.data

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("data")
    val suggestion: List<LocationData>
)
//suggestion: Список объектов LocationData, представляющих предложения по местоположению.
