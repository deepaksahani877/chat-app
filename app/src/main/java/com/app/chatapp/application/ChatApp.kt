package com.app.chatapp.application

import android.app.Application
import com.google.firebase.database.FirebaseDatabase


class ChatApp: Application() {
    override fun onCreate() {
        super.onCreate()
        val firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseDatabase.setPersistenceEnabled(true)
        val firebaseDatabaseReference = firebaseDatabase.reference
        firebaseDatabaseReference.keepSynced(true)
    }
}