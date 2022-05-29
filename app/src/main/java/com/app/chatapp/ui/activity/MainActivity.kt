package com.app.chatapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.chatapp.R
import com.app.chatapp.databinding.ActivityMainBinding
import com.app.chatapp.ui.dialogs.AddUserToActiveChatDialog
import com.app.chatapp.ui.fragments.ChatFragment
import com.app.chatapp.ui.fragments.ProfileFragment
import com.app.chatapp.ui.fragments.StatusFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var arrayOfTabsName = arrayOf("CHATS", "STATUS")
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.included.toolbar)
//        supportActionBar!!.setHomeButtonEnabled(true)
//        binding.included.toolbar.title = resources.getString(R.string.app_name)
//        binding.included.toolbar.inflateMenu(R.menu.menu)

        if(savedInstanceState==null){
            setCurrentFragment(ChatFragment())
        }
        binding.bottomNavigation.setOnItemSelectedListener {menuItem->
            when(menuItem.itemId){
                R.id.chatPage->{
                    setCurrentFragment(ChatFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.statusPage->{
                    setCurrentFragment(StatusFragment())
                    return@setOnItemSelectedListener true
                }
                else->{
                    setCurrentFragment(ProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.searchMenu -> {
                //Search
                return true
            }

            R.id.newChat -> {
                addUserToActiveChats()
                return true
            }
            R.id.profileMenu -> {
                //profile
                startActivity(Intent(applicationContext,UserProfile::class.java))
                return true
            }
            R.id.logOut -> {
                auth.signOut()
                Toast.makeText(applicationContext, "singOut", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, GetStartedActivity::class.java))
                finish()
                return true

            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment,fragment)
            commit()
        }

    private fun addUserToActiveChats() {
        val addUserToActiveChatDialog = AddUserToActiveChatDialog()
        addUserToActiveChatDialog.show(supportFragmentManager, "addUserToActiveChatDialog")
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(applicationContext, GetStartedActivity::class.java))
            finish()
        } else {
            val dbReference = FirebaseDatabase.getInstance().reference
            dbReference.child("UsersData")
                .child(FirebaseAuth.getInstance().currentUser!!.phoneNumber!!).child("userName")
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null || snapshot.value.toString() == "") {
                            Toast.makeText(applicationContext,"User Name cannot be Empty",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(applicationContext, UserNameUpdate::class.java))
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

        }
    }

}