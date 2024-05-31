package com.swamy3697.examgpt.response

data class ChatRequest(
    val messages: List<Message>,
    val model: String

)