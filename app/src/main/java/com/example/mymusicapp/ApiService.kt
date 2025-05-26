package com.example.mymusicapp

import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("songs") // Replace with your actual API endpoint path
    suspend fun getSongs(): Response<List<Song>>
}