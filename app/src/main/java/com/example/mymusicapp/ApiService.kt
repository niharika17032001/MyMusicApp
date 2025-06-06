package com.example.mymusicapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query // Import Query annotation

interface ApiService {
    @GET("songs") // Replace with your actual API endpoint path
    suspend fun getSongs(
        @Query("page") page: Int, // Add page query parameter
        @Query("limit") limit: Int // Add limit query parameter
    ): Response<List<Song>>
}