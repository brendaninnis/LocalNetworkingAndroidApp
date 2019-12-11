package com.example.localnetworkingandroidapp

import com.google.gson.Gson

class Message(val sender: String, val message: String, val timestamp: Long) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        // Sender values
        const val SERVER_MSG_SENDER = "SERVER_MSG_SENDER"
        const val SERVER_NAME_SENDER = "SERVER_NAME_SENDER"

        @JvmStatic
        fun fromJson(json: String): Message = Gson().fromJson(json, Message::class.java)
    }
}