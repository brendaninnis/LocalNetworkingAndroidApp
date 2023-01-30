package com.example.localnetworkingandroidapp.data.channel

import android.util.Log
import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.CanonicalThread
import com.example.localnetworkingandroidapp.model.SendMessage
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

internal class ChannelToClientsImplementation(
    val client: Client,
    val canonicalThread: CanonicalThread,
): ChannelService {
    var isOpen = false
    val TAG = "ChannelToClient"

    private suspend fun read() = withContext(Dispatchers.IO) {
        var line: String?
        val reader: BufferedReader

        try {
            reader = BufferedReader(InputStreamReader(client.socket.getInputStream()))
        } catch (e: IOException) {
            Log.e(TAG, "ERROR BufferedReader IO")
            close()
            return@withContext
        }

        while (isOpen) {
            try {
                line = reader.readLine()

                if (line == null) {
                    close()
                    break
                }

                Log.d(TAG, "Read line $line")

                val message = Message.fromJson(line)
                canonicalThread.addMessage(message)
                SendMessage(message).toAllClients(exception = client)
            } catch (e: IOException) {
                close()
                return@withContext
            }
        }
    }

    override suspend fun open() {
        withContext(Dispatchers.IO) {
            isOpen = true
            read()
        }
    }

    override suspend fun close() {
        isOpen = false
        if (WifiConnectionState.connectedClients.remove(client)) {
            canonicalThread.addLeaverMessage(client)
        } else { Log.e(TAG, "ERROR : close a channel to an non connected client") }
    }
}