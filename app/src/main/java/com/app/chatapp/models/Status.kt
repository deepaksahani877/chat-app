package com.app.chatapp.models

class Status {

    var user:User? = null
    var story:Story? = null
    constructor(user:User,story:Story ){

        this.user = user
        this.story = story
    }

    constructor() {}
}