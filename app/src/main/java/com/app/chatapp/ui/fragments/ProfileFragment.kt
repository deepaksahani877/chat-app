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
import com.app.chatapp.databinding.ProfileFragmentBinding
import com.app.chatapp.models.User
import com.app.chatapp.ui.activity.GetStartedActivity
import com.app.chatapp.ui.dialogs.LoadingDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class ProfileFragment : Fragment() {
    private lateinit var binding: ProfileFragmentBinding
    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var activityForResult: ActivityResultLauncher<Intent>
    private val storageReference = FirebaseStorage.getInstance().reference

    lateinit var name: String
    lateinit var phone: String
    var profilePictureUrl: String? = null
    private var selectedImageUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProfileFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    //  you will get result here in result.data
                    if (result.data != null) {
                        selectedImageUri = result.data!!.data
                        Glide.with(requireActivity()).load(selectedImageUri)
                            .into(binding.profileImage)
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

        binding.profileImage.setOnClickListener {
            fileChooser()
        }


        databaseReference.child("UsersData").child(auth.currentUser?.phoneNumber!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val user = snapshot.getValue(User::class.java)
                        name = user!!.userName
                        phone = user.phoneNumber
                        profilePictureUrl = user.profilePictureUrl
                        setUserData()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context,error.message,Toast.LENGTH_SHORT).show()
                }

            })

        binding.save.setOnClickListener {
            updateProfile()
        }


        binding.logOutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "singOut", Toast.LENGTH_SHORT).show()
            startActivity(Intent(context, GetStartedActivity::class.java))
            activity?.finish()
        }

    }


    private fun fileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        activityForResult.launch(intent)

    }


    private fun updateProfile() {
        val dialog = LoadingDialog("Uploading Image...")
        dialog.show(requireActivity().supportFragmentManager,"uploadingImageDialog")
        if (selectedImageUri != null) {
            storageReference.child("ProfileImages").child(auth.currentUser?.phoneNumber.toString())
                .child("profile/profileImg.jpeg").putFile(
                    selectedImageUri!!
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        storageReference.child("ProfileImages")
                            .child(auth.currentUser?.phoneNumber.toString())
                            .child("profile/profileImg.jpeg").downloadUrl.addOnCompleteListener { t ->
                                val uri = t.result.toString()
                                databaseReference.child("UsersData")
                                    .child(auth.currentUser?.phoneNumber.toString())
                                    .child("profilePictureUrl").setValue(uri)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT)
                                                .show()
                                            dialog.dismiss()
                                        }
                                    }
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

    fun setUserData() {
        binding.phone.text = phone
        binding.userName.text = name
        if (profilePictureUrl != "null") {
            if(isAdded){
                Glide.with(requireActivity()).load(profilePictureUrl).into(binding.profileImage)
            }
        }
    }
}