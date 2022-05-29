package com.app.chatapp.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.app.chatapp.databinding.ActivityUserProfileBinding

class UserProfile : AppCompatActivity() {
    lateinit var binding :ActivityUserProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.included.toolbar.title = "Profile"

    }
}