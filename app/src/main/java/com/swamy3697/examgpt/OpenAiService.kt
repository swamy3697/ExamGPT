package com.swamy3697.examgpt

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Response

interface OpenAiService {
    @Headers("Authorization: Bearer proj_tu4FG8GxRiqUCCA8rRQBMraL")
    @POST("/completions")
    suspend fun getResponse(@Body request:PromptRequest):Response<ApiResponse>
}

