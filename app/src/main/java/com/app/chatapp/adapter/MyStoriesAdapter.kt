package com.app.chatapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.chatapp.databinding.StatusItemBinding
import com.app.chatapp.models.MyStories
import com.app.chatapp.ui.activity.MyStatusActivity
import com.bumptech.glide.Glide
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.model.MyStory
import java.text.SimpleDateFormat
import java.util.*

class MyStoriesAdapter(private var myStoriesList: ArrayList<MyStories>,private var context: Context) : RecyclerView.Adapter<MyStoriesAdapter.ViewHolder>() {

    class ViewHolder(itemBinding: StatusItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        var binding: StatusItemBinding = itemBinding
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = StatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val adapterPosition = holder.adapterPosition
        Glide.with(context).load(myStoriesList[adapterPosition].status.story?.statusImageUrl).into(holder.binding.profileImage)
        holder.binding.userName.text = "My status "+(adapterPosition+1)
        holder.binding.timeStamp.text = myStoriesList[adapterPosition].status.story?.timeStamp
        holder.binding.constraintLayout.setOnClickListener{
            val storyList = ArrayList<MyStory>()
            storyList.add(MyStory(myStoriesList[adapterPosition].status.story?.statusImageUrl,
                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(myStoriesList[adapterPosition].status.story?.timeStamp!!)))
            val storyBuilder = StoryView.Builder((context as MyStatusActivity).supportFragmentManager)
                .setStoriesList(storyList)
                .setStoryDuration(5000)
                .setTitleText(myStoriesList[adapterPosition].status.user?.userName)
                .setTitleLogoUrl(myStoriesList[adapterPosition].status.user?.profilePictureUrl)
                .build()

            storyBuilder.show()

        }

    }


    override fun getItemCount(): Int {
        return myStoriesList.size
    }



    fun updateStory(list:ArrayList<MyStories>){
        myStoriesList = list
        notifyItemChanged(myStoriesList.size-1)
    }
}