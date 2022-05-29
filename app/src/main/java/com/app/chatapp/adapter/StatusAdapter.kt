package com.app.chatapp.adapter

import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.chatapp.R
import com.app.chatapp.databinding.StatusItemBinding
import com.app.chatapp.models.Statuses
import com.app.chatapp.ui.activity.MyStatusActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class StatusAdapter(private var statusArrayList: ArrayList<Statuses>) :
    RecyclerView.Adapter<StatusAdapter.ViewHolder>() {


    private lateinit var itemClickListener: OnItemClickListener
    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(itemBinding: StatusItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        var binding: StatusItemBinding = itemBinding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.binding.circularStatusView.context
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val currentTime =
            simpleDateFormat.parse(statusArrayList[holder.adapterPosition].listOfStories[statusArrayList[holder.adapterPosition].listOfStories.size - 1].timeStamp)?.time

        val timestamp = DateUtils.getRelativeTimeSpanString(
            currentTime!!,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )

        holder.binding.timeStamp.text = timestamp

        if (auth.currentUser?.phoneNumber == statusArrayList[holder.adapterPosition].user.phoneNumber) {
            holder.binding.userName.text = context.resources.getString(R.string.myStatus)
            holder.binding.myStatusMenu.visibility = VISIBLE
            holder.binding.myStatusMenu.setOnClickListener{
                val intent = Intent(context,MyStatusActivity::class.java)
                context.startActivity(intent)
            }
        } else {
            holder.binding.userName.text = statusArrayList[holder.adapterPosition].user.userName
        }
        val lastStoryImageUrl = statusArrayList[holder.adapterPosition]
            .listOfStories[statusArrayList[holder.adapterPosition]
            .listOfStories.size-1].statusImageUrl
        if(lastStoryImageUrl!=""){
            Glide.with(context).load(lastStoryImageUrl)
                .into(holder.binding.profileImage)
        }
        else{
            Glide.with(context).load(R.drawable.ic_user)
                .into(holder.binding.profileImage)
        }


        holder.binding.circularStatusView.setPortionsCount(statusArrayList[holder.adapterPosition].listOfStories.size)

        if (this::itemClickListener.isInitialized) {
            holder.binding.constraintLayout.setOnClickListener {
                itemClickListener.onItemClick(holder.adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return statusArrayList.size
    }


    fun update(arrayList: ArrayList<Statuses>) {
        statusArrayList = arrayList
        notifyItemChanged(statusArrayList.size - 1)
    }


    fun setOnItemClickListener(onClick: OnItemClickListener) {
        this.itemClickListener = onClick
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

}

