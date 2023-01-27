package com.example.localnetworkingandroidapp.model

import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.WifiConnectionState.connectedClients
import com.example.localnetworkingandroidapp.model.WifiConnectionState.socket
import com.example.localnetworkingandroidapp.model.WifiConnectionState.writer
import java.io.PrintWriter

class SendMessage(private val message: Message, private val canonicalThread: CanonicalThread) {
    fun toAllClients(exception: Client? = null) {
        connectedClients.forEach { _client ->
            exception?.let { _exception ->
                if (_exception != _client) {
                    send(_client.writer)
                }
            } ?: let {
                send(_client.writer)
            }
        }
    }

    fun toServer() {
        socket?.let {
            writer?.print(message.toJson() + Message.MESSAGE_TERMINATOR)
            writer?.flush()
            canonicalThread.addMessage(message)
        }
    }

    private fun send(printWriter: PrintWriter) {
        printWriter.print(message.toJson() + Message.MESSAGE_TERMINATOR)
        printWriter.flush()
        canonicalThread.addMessage(message)
    }
}