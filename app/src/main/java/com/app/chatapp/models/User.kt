package com.app.chatapp.models

class User {
    var userName: String = ""
    var phoneNumber: String = ""
    var userUid: String = ""
    var profilePictureUrl: String = ""

    constructor(
        userName: String,
        phoneNumber: String,
        userUid: String,
        profilePictureUrl: String,
    ) {
        this.userName = userName
        this.phoneNumber = phoneNumber
        this.userUid = userUid
        this.profilePictureUrl = profilePictureUrl
    }

    constructor()
}
