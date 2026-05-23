package com.example.homeautomation.network

import com.example.homeautomation.model.House
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService {
    @POST("login")
    suspend fun login(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("register")
    suspend fun register(@Body body: Map<String, String>): Response<Map<String, Any>>

    @GET("config/{username}")
    suspend fun getConfig(@Path("username") username: String): Response<List<House>>

    @POST("add_element")
    suspend fun addElement(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("update_status")
    suspend fun updateStatus(@Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("delete_element")
    suspend fun deleteElement(@Body body: Map<String, String>): Response<Map<String, Any>>

    // Generic GET for external APIs (Weather & Geocoding)
    @GET
    suspend fun getExternalData(@Url url: String): Response<Map<String, Any>>
}
