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

internal class ChannelToClientImplementation(
    val client: Client,
    private val clientList: List<Client>,
    val canonicalThread: CanonicalThread,
): ChannelService {
    private var isOpen = false
    val TAG = "ChannelToClient"

    private suspend fun toClient() = withContext(Dispatchers.IO) {
        Log.e(TAG, "toClient ${client.name} ")
        var line: String?
        val reader: BufferedReader

        try {
            reader = BufferedReader(InputStreamReader(client.socket.getInputStream()))
        } catch (e: IOException) {
            Log.e(TAG, "ERROR BufferedReader IO")
            WifiConnectionState.removeClient(client)
            return@withContext
        }

        while (isClientConnected()) {
            try {
                line = reader.readLine()

                if (line == null) {
                    WifiConnectionState.removeClient(client)
                    break
                }

                Log.d(TAG, "Read line $line")

                val message = Message.fromJson(line)
                canonicalThread.addMessage(message)
                SendMessage(message, canonicalThread).toAllClients(exception = client)
            } catch (e: IOException) {
                WifiConnectionState.removeClient(client)
                return@withContext
            }
        }
        Log.e(TAG, "toClient ${client.name} end")
    }

    override suspend fun open() {
        Log.e(TAG, "open")
        withContext(Dispatchers.IO) {
            isOpen = true
            toClient()
        }
    }

    override suspend fun close() {
    }
    private fun isClientConnected(): Boolean = clientList.contains(client)
}