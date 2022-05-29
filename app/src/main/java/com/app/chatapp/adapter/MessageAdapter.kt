package com.app.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.chatapp.R
import com.app.chatapp.models.MessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class MessageAdapter(
    messageList: ArrayList<MessageModel>,
    currentUserUID: String,
    receiverUid: String,
    receiverPhone:String

) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private var messageList: ArrayList<MessageModel>
    private var currentUserUID: String
    private var receiverUid: String
    private var status: Boolean
    private var receiverType: Int
    private var senderType: Int
    private var receiverPhone:String


    private val auth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference


    init {
        this.messageList = messageList
        this.currentUserUID = currentUserUID
        this.receiverUid = receiverUid
        this.receiverPhone = receiverPhone
        receiverType = 2
        senderType = 1
        status = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == senderType) {
            LayoutInflater.from(parent.context).inflate(R.layout.message_send, parent, false)
        } else {
            LayoutInflater.from(parent.context).inflate(R.layout.message_receive, parent, false)
        }
        return ViewHolder(view, status)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView?.text = messageList[holder.adapterPosition].message
        holder.timeStamp?.text = messageList[holder.adapterPosition].timeStamp
        if (messageList[holder.adapterPosition].fromUID == auth.uid) {
            val onContextMenuListenerForSender =
                View.OnCreateContextMenuListener { menu, v, _ ->
                    if (menu != null && v != null) {
                        menu.setHeaderTitle("Delete Message")
                        menu.add(holder.adapterPosition, 1, 0, "Delete for Everyone")
                            .setOnMenuItemClickListener {
                                databaseReference.child("Messages")
                                    .child(auth.currentUser!!.uid).child(receiverUid)
                                    .child(messageList[holder.adapterPosition].key!!).removeValue()

                                databaseReference.child("Messages")
                                    .child(receiverUid).child(auth.currentUser!!.uid)
                                    .child(messageList[holder.adapterPosition].key!!).removeValue()

                                messageList.removeAt(holder.adapterPosition)
                                notifyItemRemoved(holder.adapterPosition)
                                notifyItemRangeChanged(position, messageList.size)
                                if(messageList.size>1){
                                    databaseReference.child("ActiveChats")
                                        .child(auth.currentUser?.phoneNumber!!).child(receiverPhone)
                                        .child("lastMessage")
                                        .setValue(messageList[messageList.size-1].message)
                                    databaseReference.child("ActiveChats")
                                        .child(receiverPhone)
                                        .child(auth.currentUser?.phoneNumber!!)
                                        .child("lastMessage")
                                        .setValue(messageList[messageList.size-1].message)
                                }
                                else{
                                    databaseReference.child("ActiveChats")
                                        .child(auth.currentUser?.phoneNumber!!).child(receiverPhone)
                                        .child("lastMessage")
                                        .setValue("")
                                    databaseReference.child("ActiveChats")
                                        .child(receiverPhone)
                                        .child(auth.currentUser?.phoneNumber!!)
                                        .child("lastMessage")
                                        .setValue("")
                                }

                                true
                            }
                        menu.add(holder.adapterPosition, 0, 0, "Delete for Me")
                            .setOnMenuItemClickListener {
                                databaseReference.child("Messages")
                                    .child(auth.currentUser!!.uid).child(receiverUid)
                                    .child(messageList[holder.adapterPosition].key!!).removeValue()

                                messageList.removeAt(holder.adapterPosition)
                                notifyItemRemoved(holder.adapterPosition)
                                notifyItemRangeChanged(position, messageList.size)
                                databaseReference.child("ActiveChats").child(auth.currentUser?.phoneNumber!!).child(receiverPhone).child("lastMessage").setValue(messageList[messageList.size-1].message)

                                true
                            }
                    }
                }
            holder.textView?.setOnCreateContextMenuListener(onContextMenuListenerForSender)
        } else {
            val onContextMenuListenerForSender =
                View.OnCreateContextMenuListener { menu, v, _ ->
                    if (menu != null && v != null) {
                        menu.setHeaderTitle("Delete Message")
                        menu.add(holder.adapterPosition, 0, 0, "Delete for Me")
                            .setOnMenuItemClickListener {
                                databaseReference.child("Messages")
                                    .child(auth.currentUser!!.uid).child(receiverUid)
                                    .child(messageList[holder.adapterPosition].key!!).removeValue()

                                messageList.removeAt(holder.adapterPosition)
                                notifyItemRemoved(holder.adapterPosition)
                                notifyItemRangeChanged(position, messageList.size)

                                true
                            }
                    }
                }
            holder.textView?.setOnCreateContextMenuListener(onContextMenuListenerForSender)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].fromUID.equals(currentUserUID)) {
            status = true
            senderType
        } else {
            status = false
            receiverType
        }
    }

    fun updateMessage(messageList: ArrayList<MessageModel>) {
        this.messageList = messageList
        notifyItemChanged(messageList.size - 1)
    }

    class ViewHolder(itemView: View, status: Boolean) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView? = null
        var timeStamp :TextView ? = null


        init {
            textView = if (status) {
                timeStamp = itemView.findViewById(R.id.timeStampTextView)
                itemView.findViewById(R.id.messageSentTextView)
            } else {
                timeStamp = itemView.findViewById(R.id.timeStampTextView)
                itemView.findViewById(R.id.messageReceiveTextView)
            }

        }
    }

}
