package com.app.chatapp.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chatapp.R
import com.app.chatapp.adapter.StatusAdapter
import com.app.chatapp.adapter.StatusAdapter.OnItemClickListener
import com.app.chatapp.databinding.StatusFragmentBinding
import com.app.chatapp.models.*
import com.app.chatapp.ui.dialogs.StatusPreviewFragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.model.MyStory
import java.text.SimpleDateFormat
import java.util.*


class StatusFragment : Fragment() {
    private lateinit var binding: StatusFragmentBinding
    private var statusArrayList = ArrayList<Statuses>()
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var statusRecyclerView: RecyclerView
    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var activityForResult: ActivityResultLauncher<Intent>
    private var selectedImageUri: Uri? = null
    var myContactStatuses = ArrayList<Statuses>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StatusFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusRecyclerView = binding.statusRecyclerView
        statusRecyclerView.layoutManager = LinearLayoutManager(context)

        statusAdapter = StatusAdapter(statusArrayList)
        statusRecyclerView.adapter = statusAdapter

        databaseReference.child("UsersData").child(auth.currentUser?.phoneNumber!!).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
              val currentUser = snapshot.getValue(User::class.java)
                if(currentUser?.profilePictureUrl!="null"){
                    Glide.with(context!!).load(currentUser?.profilePictureUrl).into(binding.circleImageView)
                }
                else{
                    Glide.with(context!!).load(R.drawable.ic_user).into(binding.circleImageView)
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        activityForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    if (result.data != null) {
                        selectedImageUri = result.data!!.data
                        val dialog = StatusPreviewFragment()
                        val bundle = Bundle()
                        bundle.putString("ImageUri", selectedImageUri.toString())
                        dialog.arguments = bundle
                        dialog.show(
                            requireActivity().supportFragmentManager,
                            "StatusPreviewFragment"
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "No image selected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show()
                }
            }

        fetchStatuses()


        binding.addStatusConstraintLayout.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activityForResult.launch(intent)
        }


        statusAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val myStories: ArrayList<MyStory> = ArrayList()
                val user = myContactStatuses[position].user
                for (story in myContactStatuses[position].listOfStories) {
                    myStories.add(
                        MyStory(
                            story.statusImageUrl,
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(story.timeStamp)
                        )
                    )
                }

                val storyBuilder = StoryView.Builder(requireActivity().supportFragmentManager)
                    .setStoriesList(myStories)
                    .setStoryDuration(5000)
                    .setTitleText(user.userName)
                    .setTitleLogoUrl(user.profilePictureUrl)
                    .build()
                storyBuilder.show()
            }
        })
    }

    private fun fetchStatuses(){
        databaseReference.child("ActiveChats").child(auth.currentUser?.phoneNumber!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    databaseReference.child("Status").child(auth.currentUser?.phoneNumber!!).addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                val listOfStories = ArrayList<Story>()
                                var currentStatusUser : User? =null
                                for(snap in snapshot.children){
                                    val status = snap.getValue(Status::class.java)
                                    listOfStories.add(status?.story!!)
                                    currentStatusUser = status.user!!
                                }
                                myContactStatuses.add(Statuses(listOfStories,currentStatusUser!!))
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
                        }
                    })

                    if (snapshot.exists()) {
                        for (i in snapshot.children) {
                            val user = i.getValue(ActiveChatUsers::class.java)!!
                            if(user.user!!.phoneNumber!=auth.currentUser?.phoneNumber){
                                databaseReference.child("Status").child(user.user!!.phoneNumber).addValueEventListener(object :ValueEventListener{
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.exists()){
                                            val listOfStories = ArrayList<Story>()
                                            var currentStatusUser : User? =null
                                            for(snap in snapshot.children){
                                                val status = snap.getValue(Status::class.java)
                                                listOfStories.add(status?.story!!)
                                                currentStatusUser = status.user!!
                                            }
                                            myContactStatuses.add(Statuses(listOfStories,currentStatusUser!!))
                                            statusAdapter.update(myContactStatuses)
                                        }
                                        else{
                                            statusAdapter.update(myContactStatuses)
                                        }
                                    }
                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }

            })
    }


}