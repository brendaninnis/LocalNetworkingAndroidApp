package com.example.localnetworkingandroidapp.data

import com.google.gson.Gson

class Message(from: Sender, name: String, val sender: String, val text: String, val timestamp: Long) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        // Sender values
//        const val SERVER_MSG_SENDER = "SERVER_MSG_SENDER"
//        const val SERVER_NAME_SENDER = "SERVER_NAME_SENDER"

        @JvmStatic
        fun fromJson(json: String): Message = Gson().fromJson(json, Message::class.java)

        val HOST = Sender.Host
        val CLIENT = Sender.Client
        enum class Sender {
            Host, Client
        }
    }
}
