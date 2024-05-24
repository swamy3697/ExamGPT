package com.swamy3697.examgpt

import android.app.ActionBar.LayoutParams
import android.icu.text.ListFormatter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class Adapter(private val items: MutableList<MessageResponses>): RecyclerView.Adapter<Adapter.ViewHolder>(){

    inner class ViewHolder(responseView: View):RecyclerView.ViewHolder(responseView){
        val messageParent: ConstraintLayout =responseView.findViewById(R.id.messageParent)
        val icon:ShapeableImageView=responseView.findViewById(R.id.userIcon)
        val user:TextView=responseView.findViewById(R.id.userName)
        val textResponses: TextView=responseView.findViewById(R.id.userMessage)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.message,parent,false);
        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textResponses.text=items.get(position).text
        //var layoutParams=holder.messageParent.layoutParams as LinearLayout.LayoutParams

        if (items.get(position).user.equals("user")){
            holder.user.text="user"
            holder.icon.setImageResource(R.drawable.swamy)
           // holder.textResponses.setBackgroundResource(R.drawable.bg_for_text_response_from_user)
        }
        else{
            holder.user.text="ExamGPT"
            holder.textResponses.width=LayoutParams.WRAP_CONTENT
            holder.icon.setImageResource(R.drawable.icon)

        }
       // holder.messageParent.layoutParams=layoutParams
    }
    fun insertUserPromt(promt:MessageResponses){
        items.add(promt)
        notifyItemInserted(items.size - 1)

    }

}