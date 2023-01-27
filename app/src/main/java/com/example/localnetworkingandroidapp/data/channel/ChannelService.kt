package com.example.localnetworkingandroidapp.data.channel

import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.model.CanonicalThread
import java.net.Socket

interface ChannelService {
    suspend fun open()
    suspend fun close()

    companion object {
        fun createChannelToServer(socket: Socket, canonicalThread: CanonicalThread): ChannelService {
            return ChannelToServerImplementation(socket, canonicalThread)
        }
        fun createChannelToClient(client: Client, clientList: MutableList<Client>, canonicalThread: CanonicalThread): ChannelService {
            return ChannelToClientImplementation(client, clientList, canonicalThread)
        }
    }
}