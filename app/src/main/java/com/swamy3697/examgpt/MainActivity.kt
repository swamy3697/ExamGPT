package com.swamy3697.examgpt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory




lateinit var  adapter:Adapter
lateinit var recyclerView:RecyclerView
lateinit var retrofit: Retrofit
var openAiService: OpenAiService? = null

lateinit var promts_and_responses_list:MutableList<MessageResponses>
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        promts_and_responses_list= mutableListOf()

        recyclerView=findViewById(R.id.container)
        val layoutmanner=LinearLayoutManager(this)
        recyclerView.layoutManager=layoutmanner


        adapter=Adapter(promts_and_responses_list)
        recyclerView.adapter=adapter


        val promt:EditText=findViewById(R.id.userpromt)




        val  sendPromtBtn:ImageButton=findViewById(R.id.sendPromt)
        sendPromtBtn.setOnClickListener {
            if (promt.text.isEmpty()) {
                return@setOnClickListener
            }

            makePromt(promt.text.toString(),"user")
            promt.setText("")
            Toast.makeText(this, "ok", Toast.LENGTH_LONG).show()
        }


        retrofit=Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        openAiService = retrofit.create(OpenAiService::class.java)



    }//end main acitvity oncreate



    private fun makePromt(text: String, personType: String) {
        val request = PromptRequest(text)
        // Using coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = openAiService?.getResponse(request)
                withContext(Dispatchers.Main) {
                    if (response != null && response.isSuccessful) {


                        val choice = response.body()?.choices?.getOrNull(0)
                        Toast.makeText(this@MainActivity, "1", Toast.LENGTH_SHORT).show()
                        val responseText = choice?.text ?: "No response"
                        Toast.makeText(this@MainActivity, "1", Toast.LENGTH_SHORT).show()
                        val responsePrompt = MessageResponses(responseText, "ExamGPT")

                        adapter.insertUserPromt(responsePrompt)
                        recyclerView.scrollToPosition(adapter.itemCount - 1)
                    } else {
                        if(response==null){
                            Toast.makeText(this@MainActivity, "null", Toast.LENGTH_SHORT).show()
                        }
                        if (response != null) {
                            val t:String=response.body()?.choices?.get(0).toString()
                            Toast.makeText(this@MainActivity, t, Toast.LENGTH_SHORT).show()

                        }
                        // Handle error
                        Toast.makeText(this@MainActivity, "Error: Unable to get response", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Handle exception
                withContext(Dispatchers.Main) {
                    ok(e.message)
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<ConstraintLayout>(R.id.iconHolder).visibility = View.GONE
        val prompt = MessageResponses(text, personType)
        adapter.insertUserPromt(prompt)
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun ok(message: String?) {
        val prompt = MessageResponses(message.toString(),"ai")
        adapter.insertUserPromt(prompt)
        recyclerView.scrollToPosition(adapter.itemCount - 1)

    }

}