package com.swamy3697.examgpt

import com.swamy3697.examgpt.response.ChatRequest
import com.swamy3697.examgpt.response.ChatResponse
import retrofit2.http.POST

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header

interface ApiInterface {

    @POST("chat/completions")
     fun createChatCompletion(
        @Body chatRequest: ChatRequest,
        @Header("Content-Type") contentType:String="application/json",
        @Header("Authorization") authorization:String="Bearer $API_KEY"
    ):Call<ChatResponse>
}