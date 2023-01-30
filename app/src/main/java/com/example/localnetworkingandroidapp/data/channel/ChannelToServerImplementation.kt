package com.example.localnetworkingandroidapp.data.channel

import android.util.Log
import com.example.localnetworkingandroidapp.data.LinkStates
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.CanonicalThread
import com.example.localnetworkingandroidapp.model.Names
import com.example.localnetworkingandroidapp.model.WifiConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket

internal class ChannelToServerImplementation(
    private val socket: Socket,
    private val canonicalThread: CanonicalThread
): ChannelService {
    private var isOpen = false
    val TAG = "ChannelToServer"

    private suspend fun read() = withContext(Dispatchers.IO) {
        var line: String?
        val reader: BufferedReader

        try {
            reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        } catch (e: IOException) {
            println("in or out failed")
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

                if (message.sender == Message.SERVER_NAME_SENDER) {
                    Names.deviceName = message.text
                    continue
                }

                canonicalThread.addMessage(message)
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

    override suspend fun close() = withContext(Dispatchers.IO) {
        WifiConnectionState.socket?.close()
        WifiConnectionState.socket = null
        WifiConnectionState.updateLinkStateTo(LinkStates.NotConnected)
        WifiConnectionState.changeBottomBarStateTo(false)
        isOpen = false
        canonicalThread.reset()
    }
}