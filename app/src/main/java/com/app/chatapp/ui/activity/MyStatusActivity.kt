package com.app.chatapp.ui.activity

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.chatapp.R
import com.app.chatapp.adapter.MyStoriesAdapter
import com.app.chatapp.databinding.ActivityMyStatusBinding
import com.app.chatapp.models.MyStories
import com.app.chatapp.models.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class MyStatusActivity : AppCompatActivity() {
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    lateinit var binding:ActivityMyStatusBinding
    private var listOfStories = ArrayList<MyStories>()
    lateinit var adapter : MyStoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adapter = MyStoriesAdapter(listOfStories,this)
        binding.myStatusListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.myStatusListRecyclerView.adapter = adapter

        enableSwipeToDelete()

        databaseReference.child("Status").child(auth.currentUser?.phoneNumber!!)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        val story = snapshot.getValue(Status::class.java)
                        val key = snapshot.key
                        if (story != null && key!=null) {
                            listOfStories.add(MyStories(story,key))
                            adapter.updateStory(listOfStories)
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onChildRemoved(snapshot: DataSnapshot) {}

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(error: DatabaseError) {}
            })
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
                databaseReference.child("Status").child(auth.currentUser?.phoneNumber!!).child(listOfStories[position].key).removeValue()
                listOfStories.removeAt(position)
                adapter.updateStory(listOfStories)
                adapter.notifyItemRemoved(position)
                Toast.makeText(applicationContext,"Removed",Toast.LENGTH_SHORT).show()

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
                val icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_delete)!!
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
        itemTouchHelper.attachToRecyclerView(binding.myStatusListRecyclerView)
    }
}