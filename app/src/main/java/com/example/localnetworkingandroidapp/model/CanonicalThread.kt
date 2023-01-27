package com.example.localnetworkingandroidapp.model

import android.util.Log
import com.example.localnetworkingandroidapp.data.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CanonicalThread {
    private val TAG = "CanonicalThread"
    private val _messageList = MutableStateFlow(listOf<Message>())
    val messageList: StateFlow<List<Message>> = _messageList.asStateFlow()

    fun addMessage(newMessage: Message) {
        Log.v(TAG, "add message from : ${newMessage.sender}, text: ${newMessage.text}, date: ${newMessage.timestamp}")
        //creating a new list to force screen ui recomposition
        val newList = _messageList.value.toMutableList()
        newList.add(newMessage)
        _messageList.value = newList
    }

    fun reset() {
        _messageList.value = listOf<Message>()
    }
}