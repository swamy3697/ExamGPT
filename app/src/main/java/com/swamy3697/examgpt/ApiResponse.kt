package com.swamy3697.examgpt

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("choices")
    val choices: List<Choice>
)
data class Choice(

    @SerializedName("text")
    val text: String
)
