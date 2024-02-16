package com.example.Interface

import com.example.data.LocationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SuggestApi {
    @GET("utils/suggest")
    suspend fun getCity(
        @Query("text") query: String
    ): LocationResponse
}