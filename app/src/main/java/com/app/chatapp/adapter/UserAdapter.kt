package com.app.chatapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.app.chatapp.R
import com.app.chatapp.databinding.UserListItemBinding
import com.app.chatapp.models.ActiveChatUsers
import com.app.chatapp.models.User
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var userDataArray: ArrayList<ActiveChatUsers>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private lateinit var itemClickListener: OnItemClickListener
    lateinit var context: Context
    class ViewHolder(view: UserListItemBinding) : RecyclerView.ViewHolder(view.root) {
        val userNameTextView: TextView = view.userName
        val lastMessageTextView: TextView = view.lastMessageTextView
        val userProfileImageView: CircleImageView = view.profileImage
        val linearLayout: ConstraintLayout = view.userListLinearLayout

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = UserListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        context = holder.lastMessageTextView.context
        val activeUser:ActiveChatUsers = userDataArray[holder.adapterPosition]
        val user: User = userDataArray[holder.adapterPosition].user!!
       holder.lastMessageTextView.text = activeUser.lastMessage

        holder.userNameTextView.text = user.userName
        if (user.profilePictureUrl != "null") {
            Glide.with(holder.userProfileImageView.context).load(user.profilePictureUrl)
                .into(holder.userProfileImageView)
        } else {
            Glide.with(holder.userProfileImageView.context).load(R.drawable.ic_user)
                .into(holder.userProfileImageView)
        }

        holder.userProfileImageView.setOnClickListener {
            Toast.makeText(
                holder.userProfileImageView.context,
                "clicked on profile pic",
                Toast.LENGTH_SHORT
            ).show()
        }
        holder.linearLayout.setOnClickListener {
            itemClickListener.onItemClick(holder.adapterPosition)
        }

    }

    override fun getItemCount(): Int {
        return userDataArray.size
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        itemClickListener = onItemClickListener
    }

    fun updateUsers(user: ArrayList<ActiveChatUsers>) {
        userDataArray = user
        notifyItemChanged(user.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterUsers(newData: ArrayList<ActiveChatUsers>){
        userDataArray= newData
        notifyDataSetChanged()

    }
}

interface OnItemClickListener {
    fun onItemClick(position: Int)
}

