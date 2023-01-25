package com.example.localnetworkingandroidapp.data.channel

import android.util.Log
import com.example.localnetworkingandroidapp.data.channel.ChannelService
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket

internal class ChannelToServerImplementation(private val socket: Socket): ChannelService {
    private var isOpen = false

    private suspend fun read() = withContext(Dispatchers.IO) {
        val TAG = "ServerReader"
        var line: String?
        val reader: BufferedReader

        try {
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        } catch (e: IOException) {
            println("in or out failed")

            WifiConnectionState.serverDisconnection()
            return@withContext
        }

        while (isOpen) {
            try {
                line = reader.readLine()

                if (line == null) {
                    isOpen = false
                    WifiConnectionState.serverDisconnection()
                    break
                }

                Log.d(TAG, "Read line $line")

//                val message = Message.fromJson(line)

//                if (message.sender == Message.SERVER_NAME_SENDER) {
//                    myName = message.message
//                    continue
//                }

//                runOnUiThread {
//                    addMessage(message, false)
//                }
            } catch (e: IOException) {
                WifiConnectionState.serverDisconnection()
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
