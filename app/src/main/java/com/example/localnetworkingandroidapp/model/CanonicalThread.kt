package com.example.localnetworkingandroidapp.model

import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.data.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class CanonicalThread {
    private val TAG = "CanonicalThread"
    private val _messageList = MutableStateFlow(listOf<Message>())
    val messageList: StateFlow<List<Message>> = _messageList.asStateFlow()

    fun addMessage(newMessage: Message) {
        //creating a new list to trigger screen ui recomposition
        val newList = _messageList.value.toMutableList()
        newList.add(newMessage)
        _messageList.value = newList
    }

    fun addLeaverMessage(missingClient: Client) {
        val leftMessage = Message(Message.SERVER_MSG_SENDER, "${missingClient.name} has left", Date().time)
        addMessage(leftMessage)
    }

    fun reset() {
        _messageList.value = listOf<Message>()
    }
}