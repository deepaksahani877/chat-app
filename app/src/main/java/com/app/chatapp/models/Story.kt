package com.app.chatapp.models

class Story {
    var timeStamp: String = ""
    var user:User? = null
    var statusImageUrl:String = ""
    constructor(user:User,statusImageUrl:String,timeStamp: String) {
        this.user = user
        this.statusImageUrl = statusImageUrl
        this.timeStamp = timeStamp
    }


    constructor() {}
}