package com.app.chatapp.models

class ActiveChatUsers {
    var user: User? = null
    var lastMessage: String = ""
    var lastMessageTimeStamp: String = ""

    constructor(user: User, lastMessage: String, lastMessageTimeStamp: String) {
        this.user = user
        this.lastMessage = lastMessage
        this.lastMessageTimeStamp = lastMessageTimeStamp
    }

    constructor()
}