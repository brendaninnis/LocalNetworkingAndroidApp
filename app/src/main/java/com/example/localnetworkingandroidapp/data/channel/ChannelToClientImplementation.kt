package com.example.localnetworkingandroidapp.data.channel

import android.util.Log
import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

internal class ChannelToClientImplementation(val client: Client): ChannelService {
    private var isOpen = false

    private suspend fun read() = withContext(Dispatchers.IO) {
        val TAG = "ClientReader"
        var line: String?
        val reader: BufferedReader

        try {
            reader = BufferedReader(InputStreamReader(client.socket.getInputStream()))
        } catch (e: IOException) {
            Log.e(TAG, "ERROR BufferedReader IO")
            WifiConnectionState.removeClient(client)
            return@withContext
        }

        while (isOpen) {
            try {
                line = reader.readLine()

                if (line == null) {
                    WifiConnectionState.removeClient(client)
                    break
                }

                Log.d(TAG, "Read line $line")

                val message = Message.fromJson(line)

                WifiConnectionState.connectedClients.forEach {
                    if (it != client) { // Don't send the message to the client who sent it
                        it.writer.print(line)
                        it.writer.flush()
                    }
                }
            } catch (e: IOException) {
                WifiConnectionState.removeClient(client)
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
    }
}