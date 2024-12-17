package com.example.calculator.service

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

data class HistoryItem(val expression: String, val result: String)

interface ApiService {
    @GET("history")
    fun getHistory(): Call<List<HistoryItem>>

    @POST("history")
    fun addHistory(@Body history: HistoryItem): Call<HistoryItem>

    @DELETE("history")
    fun clearHistory(): Call<Void>
}