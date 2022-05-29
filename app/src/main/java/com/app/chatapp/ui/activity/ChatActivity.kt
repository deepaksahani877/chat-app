package com.app.chatapp.ui.activity

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.chatapp.adapter.MessageAdapter
import com.app.chatapp.databinding.ActivityChatBinding
import com.app.chatapp.models.MessageModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference

    private val messageArrayList = ArrayList<MessageModel>()
    private lateinit var userName: String
    private lateinit var receiverPhoneNumber: String
    private lateinit var receiverUid: String
    private lateinit var profilePictureUrl: String

    private lateinit var messageAdapter: MessageAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName = intent.getStringExtra("userName")!!
        receiverPhoneNumber = intent.getStringExtra("phoneNumber")!!
        receiverUid = intent.getStringExtra("userUid")!!
        profilePictureUrl = intent.getStringExtra("profilePictureUrl")!!


        messageAdapter = MessageAdapter(messageArrayList, auth.currentUser!!.uid,receiverUid,receiverPhoneNumber)
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messageRecyclerView.adapter = messageAdapter

        getMessages()

        binding.userName.text = userName
        if (profilePictureUrl != "null")
            Glide.with(applicationContext).load(profilePictureUrl).into(binding.profileImage)



        binding.back.setOnClickListener {
            onBackPressed()
        }



        binding.sendMessageBtn.setOnClickListener {
            if (binding.messageEditText.text.toString() != "") {
                val message = binding.messageEditText.text.toString()
                val fromUid = auth.uid!!
                sendMessage(message, fromUid)
                binding.messageEditText.setText("")
            } else {
                Toast.makeText(applicationContext, "Message Empty!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.messageEditText.text.toString().isEmpty()) {
                    binding.sendMessageBtn.visibility = View.INVISIBLE
                } else {
                    binding.sendMessageBtn.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

    }

    private fun getMessages() {
        databaseReference.child("Messages").child(auth.currentUser!!.uid).child(receiverUid)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if(snapshot.exists()){
                        val message = snapshot.getValue(MessageModel::class.java) as MessageModel
                        messageArrayList.add(message)
                        messageAdapter.updateMessage(messageArrayList)
                        binding.messageRecyclerView.scrollToPosition(messageArrayList.size - 1)
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                    val message = snapshot.getValue(MessageModel::class.java) as MessageModel
                    //Toast.makeText(applicationContext,message.message,Toast.LENGTH_SHORT).show()
                    messageArrayList.remove(message)
                    binding.messageRecyclerView.scrollToPosition(messageArrayList.size - 1)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    TODO("Not yet implemented")
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun sendMessage(message: String, fromUid: String) {
        val key =
            databaseReference.child("Messages").child(auth.currentUser!!.uid).child(receiverUid)
                .push().key
        val simpleDateFormat = SimpleDateFormat("HH:mm", Locale.US)
        val currentTime = simpleDateFormat.format(Date())
        val messageModel = MessageModel(message, fromUid, key!!,currentTime)
        databaseReference.child("Messages").child(auth.currentUser!!.uid).child(receiverUid)
            .child(key).setValue(messageModel).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    databaseReference.child("Messages").child(receiverUid)
                        .child(auth.currentUser!!.uid)
                        .child(key).setValue(messageModel)
                    databaseReference.child("ActiveChats").child(auth.currentUser?.phoneNumber!!).child(receiverPhoneNumber).child("lastMessage").setValue(message)
                    databaseReference.child("ActiveChats").child(receiverPhoneNumber).child(auth.currentUser?.phoneNumber!!).child("lastMessage").setValue(message)
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
            }


    }
}