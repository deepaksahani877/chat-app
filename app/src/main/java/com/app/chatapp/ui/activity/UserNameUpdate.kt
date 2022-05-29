package com.app.chatapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.chatapp.databinding.ActivityUserNameUpdateBinding
import com.app.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserNameUpdate : AppCompatActivity() {
    private lateinit var binding: ActivityUserNameUpdateBinding
    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserNameUpdateBinding.inflate(layoutInflater)
        binding.included.toolbar.title = "Update Name"
        setSupportActionBar(binding.included.toolbar)

        setContentView(binding.root)
        if (auth.currentUser != null) {
            databaseReference.child("UsersData").child(auth.currentUser?.phoneNumber!!)
                .child("userName")
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value != null) {
                            binding.nameTextInputEditText.setText(snapshot.value.toString())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                    }

                })
        }

        binding.submitButton.setOnClickListener {
            if (binding.nameTextInputEditText.text.toString() == "") {
                binding.nameTextInputEditText.error = "Required"
            } else {
                updateName(binding.nameTextInputEditText.text.toString())
            }
        }

    }

    private fun updateName(name: String) {


        databaseReference.child("UsersData").child(auth.currentUser?.phoneNumber!!)
            .child("profilePictureUrl")
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        if (snapshot.exists()) {
                            val profilePictureUrl: String = snapshot.getValue(String::class.java)!!
                            val userData = if (profilePictureUrl != "null") {
                                User(
                                    name,
                                    auth.currentUser!!.phoneNumber.toString(),
                                    auth.currentUser!!.uid,
                                    profilePictureUrl
                                )
                            } else {
                                User(
                                    name,
                                    auth.currentUser!!.phoneNumber.toString(),
                                    auth.currentUser!!.uid,
                                    "null"
                                )
                            }
                            databaseReference.child("UsersData")
                                .child(auth.currentUser!!.phoneNumber!!).setValue(userData)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        startActivity(
                                            Intent(
                                                applicationContext,
                                                MainActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        applicationContext,
                                        it.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    else{
                        val userData = User(
                                name,
                                auth.currentUser!!.phoneNumber.toString(),
                                auth.currentUser!!.uid,
                                "null"
                            )
                        databaseReference.child("UsersData")
                            .child(auth.currentUser!!.phoneNumber!!).setValue(userData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    applicationContext,
                                    it.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

            })

    }
}