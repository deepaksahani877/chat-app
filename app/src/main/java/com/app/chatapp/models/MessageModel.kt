package com.app.chatapp.models


class MessageModel {
    var message: String? = ""
    var fromUID: String? = ""
    var key: String? = ""
    var timeStamp: String? = ""

    constructor(message: String?, fromUID: String?, key: String, timeStamp: String) {
        this.message = message
        this.fromUID = fromUID
        this.key = key
        this.timeStamp = timeStamp
    }

    constructor()
}
