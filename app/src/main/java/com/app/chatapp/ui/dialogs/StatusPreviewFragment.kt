package com.app.chatapp.ui.dialogs

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.app.chatapp.R
import com.app.chatapp.databinding.FragmentStatusPreviewBinding
import com.app.chatapp.models.ActiveChatUsers
import com.app.chatapp.models.Status
import com.app.chatapp.models.Story
import com.app.chatapp.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class StatusPreviewFragment : DialogFragment() {
    var binding: FragmentStatusPreviewBinding? = null
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val storageReference = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private var imgUri: String? = null
    var user: User? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.rounded_corner)
        dialog!!.setCancelable(true)
        binding = FragmentStatusPreviewBinding.inflate(LayoutInflater.from(context))
        return binding?.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imgUri = arguments?.getString("ImageUri")
        if (imgUri != null) {
            Glide.with(requireContext()).load(imgUri).into(binding?.statusPreviewImageView!!)
        } else {
            Toast.makeText(context, "null", Toast.LENGTH_SHORT).show()
        }

        binding?.sendStatusButton?.setOnClickListener {
            sendStatus()
        }
    }


    override fun onStart() {
        super.onStart()
        // val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog!!.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }


    private fun sendStatus() {
        val dialog = LoadingDialog("Uploading...")
        dialog.show(requireActivity().supportFragmentManager, "LoadingDialog")
//        val handler = Handler(Looper.getMainLooper())
//        var runnable:Runnable? = null

        if (imgUri != null) {
            val key =
                databaseReference.child("Status").child(auth.currentUser?.phoneNumber.toString())
                    .push().key
            storageReference.child("Status").child(auth.currentUser?.phoneNumber.toString())
                .child(key + "status.jpeg").putFile(
                    Uri.parse(imgUri!!)
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageReference.child("Status")
                            .child(auth.currentUser?.phoneNumber.toString())
                            .child(key + "status.jpeg").downloadUrl.addOnCompleteListener { t ->
                                val uri = t.result.toString()
                                databaseReference.child("UsersData")
                                    .child(auth.currentUser?.phoneNumber!!)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                user = snapshot.getValue(User::class.java)
                                                if (user != null) {
                                                    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                                    val currentTime = simpleDateFormat.format(Date())
                                                    val story = Story(user!!, uri, currentTime)
                                                    val status = Status(user!!,story)
                                                    databaseReference.child("Status")
                                                        .child(auth.currentUser?.phoneNumber.toString())
                                                        .child(key!!).setValue(status)
                                                        .addOnCompleteListener {
                                                            if (it.isSuccessful) {
                                                                dialog.dismiss()
                                                                Toast.makeText(
                                                                    context,
                                                                    "Success",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                databaseReference.child("ActiveChats").child(auth.currentUser?.phoneNumber!!).child(auth.currentUser?.phoneNumber!!)
                                                                    .addListenerForSingleValueEvent(object :ValueEventListener{
                                                                        override fun onDataChange(
                                                                            snapshot: DataSnapshot
                                                                        ) {
                                                                            if(!snapshot.exists()){
                                                                                databaseReference.child("UsersData").child(auth.currentUser?.phoneNumber!!).addListenerForSingleValueEvent(object :ValueEventListener{
                                                                                    override fun onDataChange(
                                                                                        snapshot: DataSnapshot
                                                                                    ) {
                                                                                       if(snapshot.exists()){
                                                                                           val currentUser = snapshot.getValue(User::class.java)
                                                                                           databaseReference.child("ActiveChats").child(auth.currentUser?.phoneNumber!!).child(auth.currentUser?.phoneNumber!!)
                                                                                               .setValue(ActiveChatUsers(currentUser!!,"",""))

                                                                                       }
                                                                                    }

                                                                                    override fun onCancelled(
                                                                                        error: DatabaseError
                                                                                    ) {

                                                                                    }

                                                                                })

                                                                            }
                                                                        }

                                                                        override fun onCancelled(
                                                                            error: DatabaseError
                                                                        ) {

                                                                        }

                                                                    })
                                                                this@StatusPreviewFragment.dismiss()
                                                            }
                                                        }
                                                }
                                                else{
                                                    dialog.dismiss()
                                                    Toast.makeText(context,"Error Occurred while uploading file",Toast.LENGTH_SHORT).show()
                                                }
                                                //handler.postDelayed(runnable!!,0)
                                                if (user == null) {
                                                    Toast.makeText(
                                                        context,
                                                        "null user",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
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

//                                runnable = object :Runnable{
//                                    override fun run() {
//
//                                    }
//
//                                }

                            }
                    }

                }.addOnFailureListener {
                    dialog.dismiss()
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }

        } else {
            dialog.dismiss()
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
        }
    }
}