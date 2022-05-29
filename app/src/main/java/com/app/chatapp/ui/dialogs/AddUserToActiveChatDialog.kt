package com.app.chatapp.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.chatapp.R
import com.app.chatapp.databinding.AddUserToActiveChatDialogBinding
import com.app.chatapp.models.ActiveChatUsers
import com.app.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddUserToActiveChatDialog : DialogFragment() {
    var binding: AddUserToActiveChatDialogBinding? = null
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)
        dialog!!.setCancelable(true)
        binding = AddUserToActiveChatDialogBinding.inflate(LayoutInflater.from(context))
        return binding?.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.closeAlertDialog?.setOnClickListener {
            this.dismiss()
        }


        binding?.addUserToActiveChatButton?.setOnClickListener {
            var isExists = true
            if (binding?.phoneTextInputEditText?.text.toString() != "") {
                val number = "+91" + binding?.phoneTextInputEditText?.text.toString()
                databaseReference.child("ActiveChats").child(auth.currentUser?.phoneNumber!!)
                    .child(number).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                isExists = false
                                databaseReference.child("UsersData")
                                    .child(number)
                                    .addValueEventListener(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val user: User? = snapshot.getValue(User::class.java)
                                            if (user != null) {
                                                val activeChatUser = ActiveChatUsers(user, "", "")
                                                databaseReference.child("ActiveChats")
                                                    .child(auth.currentUser?.phoneNumber!!)
                                                    .child(number)
                                                    .setValue(activeChatUser)
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {

                                                            if (context != null) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "Successful",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                //start
                                                                var currentUser:User? = null
                                                                databaseReference.child("UsersData").child(auth.currentUser?.phoneNumber!!).addListenerForSingleValueEvent(object :ValueEventListener{
                                                                    override fun onDataChange(
                                                                        snapshot: DataSnapshot
                                                                    ) {
                                                                        if(snapshot.exists()){
                                                                            currentUser = snapshot.getValue(User::class.java)
                                                                            databaseReference.child("ActiveChats")
                                                                                .child(number)
                                                                                .child(auth.currentUser?.phoneNumber!!)
                                                                                .setValue(ActiveChatUsers(currentUser!!,"",""))
                                                                                .addOnCompleteListener { task ->
                                                                                    if (task.isComplete) {
                                                                                        dialog?.dismiss()
                                                                                    }
                                                                                }
                                                                        }
                                                                    }

                                                                    override fun onCancelled(error: DatabaseError) {
                                                                       Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
                                                                    }

                                                                })


                                                                //end
                                                            }
                                                        }
                                                    }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "User not registered with us",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(
                                                context,
                                                error.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    })

                            } else {
                                if (isExists) {
                                    Toast.makeText(
                                        context,
                                        "This user already exists in your chat section",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                binding?.phoneTextInputEditText?.error = "Required"
            }


        }
    }


    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}