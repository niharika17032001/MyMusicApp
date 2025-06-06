package com.example.mymusicapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("songs")
    suspend fun getSongs(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("query") query: String? = null // Add optional query parameter
    ): Response<List<Song>>
}