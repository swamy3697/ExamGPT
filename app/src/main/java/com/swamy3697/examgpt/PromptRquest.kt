package com.swamy3697.examgpt

data class PromptRequest(
    val prompt: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 50
)


