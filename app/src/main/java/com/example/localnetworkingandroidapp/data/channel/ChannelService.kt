package com.example.localnetworkingandroidapp.data.channel

import com.example.localnetworkingandroidapp.data.Client
import java.net.Socket

interface ChannelService {
    suspend fun open()
    suspend fun close()

    companion object {
        fun createChannelToServer(socket: Socket): ChannelService {
            return ChannelToServerImplementation(socket)
        }
        fun createChannelToClient(client: Client): ChannelService {
            return ChannelToClientImplementation(client)
        }
    }
}