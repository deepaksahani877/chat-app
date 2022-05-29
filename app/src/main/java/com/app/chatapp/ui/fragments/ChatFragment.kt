package com.app.chatapp.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chatapp.R
import com.app.chatapp.adapter.OnItemClickListener
import com.app.chatapp.adapter.UserAdapter
import com.app.chatapp.databinding.ChatFragmentBinding
import com.app.chatapp.models.ActiveChatUsers
import com.app.chatapp.ui.activity.ChatActivity
import com.app.chatapp.ui.dialogs.AddUserToActiveChatDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*


class ChatFragment : Fragment() {
    private lateinit var binding: ChatFragmentBinding
    private var userArrayList: ArrayList<ActiveChatUsers> = ArrayList()
    private var auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var userAdapter: UserAdapter
    private var flag = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.userListRecyclerView.layoutManager = LinearLayoutManager(view.context)
        userAdapter = UserAdapter(userArrayList)
        binding.userListRecyclerView.adapter = userAdapter

        userAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("userName", userArrayList[position].user?.userName)
                intent.putExtra("phoneNumber", userArrayList[position].user?.phoneNumber)
                intent.putExtra("userUid", userArrayList[position].user?.userUid)
                intent.putExtra(
                    "profilePictureUrl",
                    userArrayList[position].user?.profilePictureUrl
                )
                startActivity(intent)
            }
        })

        binding.userSearchEditText.doOnTextChanged { _, _, _, _ ->

            searchUser(binding.userSearchEditText.text.toString().lowercase(Locale.getDefault()))

        }


        binding.addUserToActiveChatButton.setOnClickListener {
            addUserToActiveChats()
        }
        if (auth.currentUser != null) {
            getActiveChats()
        }

        enableSwipeToDelete()
    }


    private fun searchUser(query:String){
        if(query==""){
            binding.noActiveChatLinearLayout.visibility = View.GONE
            userAdapter.filterUsers(userArrayList)
        }
        else{
            val matchedList = getMatchedUser(userArrayList,query)
            userAdapter.filterUsers(matchedList)
            if(matchedList.size<1){
                binding.noActiveChatLinearLayout.visibility = View.VISIBLE
                binding.textView8.text = "No Result Found"
            }else{
                binding.noActiveChatLinearLayout.visibility = View.GONE
            }
        }

    }


    private fun getMatchedUser(userList:ArrayList<ActiveChatUsers>,query: String):ArrayList<ActiveChatUsers>{
        val newList = ArrayList<ActiveChatUsers>()
        for ( user in userList){
            if(user.user?.userName!!.lowercase(Locale.getDefault()).contains(query)){
                newList.add(user)
            }
        }
        return newList
    }

    private fun enableSwipeToDelete() {

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {

                //Remove swiped item from list and notify the RecyclerView
                val position = viewHolder.adapterPosition
                databaseReference.child("ActiveChats").child(auth.currentUser?.phoneNumber!!)
                    .child(userArrayList[position].user!!.phoneNumber).removeValue()
                databaseReference.child("Messages").child(auth.currentUser?.uid!!)
                    .child(userArrayList[position].user!!.userUid).removeValue()
                userArrayList.removeAt(position)
                userAdapter.updateUsers(userArrayList)
                userAdapter.notifyItemRemoved(position)
                Toast.makeText(
                    this@ChatFragment.context,
                    "Removed ",
                    Toast.LENGTH_SHORT
                ).show()

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val icon = ContextCompat.getDrawable(userAdapter.context, R.drawable.ic_delete)!!
                val background = ColorDrawable(Color.RED)
                val backgroundCornerOffset = 20
                val itemView = viewHolder.itemView
                val iconMargin: Int = (itemView.height - icon.intrinsicHeight) / 2
                val iconTop: Int = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                val iconBottom: Int = iconTop + icon.intrinsicHeight
                if (dX < 0) { // Swiping to the left
                    val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    background.setBounds(
                        itemView.right + dX.toInt() - backgroundCornerOffset,
                        itemView.top, itemView.right, itemView.bottom
                    )
                } else { // view is unSwiped
                    background.setBounds(0, 0, 0, 0)
                }

                background.draw(c)
                icon.draw(c)

            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.userListRecyclerView)
    }


    private fun addUserToActiveChats() {
        val addUserToActiveChatDialog = AddUserToActiveChatDialog()
        addUserToActiveChatDialog.show(
            requireActivity().supportFragmentManager,
            "addUserToActiveChatDialog"
        )
    }

    private fun getActiveChats() {

        databaseReference.child("ActiveChats")
            .child(auth.currentUser!!.phoneNumber!!).addChildEventListener(object :
                ChildEventListener {
                override fun onChildAdded(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    if (snapshot.exists()) {
                        val activeUser = snapshot.getValue(ActiveChatUsers::class.java)
                        if (activeUser!!.user?.phoneNumber != auth.currentUser?.phoneNumber)
                            userArrayList.add(activeUser)
                        userAdapter.updateUsers(userArrayList)
                        if (userArrayList.size > 0) {
                            binding.noActiveChatLinearLayout.visibility = View.GONE
                        } else {
                            binding.noActiveChatLinearLayout.visibility = View.VISIBLE
                        }
                    }

                }

                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
//                    if(isAdded){
//                        Toast.makeText(requireContext(),"child removed",Toast.LENGTH_SHORT).show()
//                    }
                }

                override fun onChildMoved(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        databaseReference.child("ActiveChats").child(auth.currentUser!!.phoneNumber!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        binding.noActiveChatLinearLayout.visibility = View.VISIBLE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })


    }

    override fun onPause() {
        super.onPause()
        flag = true
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (flag) {
            userArrayList.clear()
            userAdapter.notifyDataSetChanged()
            getActiveChats()
            flag = false
        }

    }

}