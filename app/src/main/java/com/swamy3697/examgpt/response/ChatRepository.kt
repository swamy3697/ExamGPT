package com.swamy3697.examgpt.response

import android.util.Log
import com.swamy3697.examgpt.AI_MODEL
import com.swamy3697.examgpt.ApiClient
import com.swamy3697.examgpt.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response

class ChatRepository {
    private val apiClient=ApiClient.getInstance()
    fun createChatCompletion(message:String,mainActivity: MainActivity){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chatRequest = ChatRequest(
                    messages = listOf(
                        Message("hello tell about yourself", "system"),
                        Message(message, "user")
                    ),
                    AI_MODEL
                )
                apiClient.createChatCompletion(chatRequest).enqueue(object :Callback<ChatResponse>{
                    override fun onResponse(
                        call: Call<ChatResponse>,
                        response: Response<ChatResponse>
                    ) {
                        val code=response.code()
                        if(code==200){
                            response.body()?.choices?.get(0)?.message.let {
                                Log.d("message",it.toString())


                            }
                            mainActivity.ok(response.body()?.choices?.get(0)?.message?.content.toString())
                        }else{
                            Log.d("error",response.errorBody().toString())
                            mainActivity.ok(response.errorBody().toString())
                            mainActivity.showToastFromChatRepository("error"+response.errorBody().toString())
                        }
                    }

                    override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                        Log.e("ChatRepository", "Failed to create chat completion", t)
                        mainActivity.ok(t.message)
                        mainActivity.showToastFromChatRepository("Failed to create chat completion: ${t.message}")
                    }

                })
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

}