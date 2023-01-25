package com.example.localnetworkingandroidapp.model

import android.content.Context
import android.net.nsd.NsdManager
import com.example.localnetworkingandroidapp.data.channel.ChannelService
import com.example.localnetworkingandroidapp.data.Client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

object WifiConnectionState {
    var socket: Socket? = null
    var serverSocket: ServerSocket? = null
    fun isHosting(): Boolean = serverSocket?.let { true } ?: false
    var connectedClients: MutableList<Client> = mutableListOf()
    lateinit var nsdManager: NsdManager
    var connected = false
    var writer: PrintWriter? = null
    val names = Names()
    var channelToServer: ChannelService? = null
    val channelToClient: ChannelService? = null

    fun init(context: Context) {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private val _bottomBarEnable = MutableStateFlow(false)
    val bottomBarEnable: StateFlow<Boolean> = _bottomBarEnable.asStateFlow()
    fun changeBottomBarStateTo(newState: Boolean) {_bottomBarEnable.value = newState}

    fun removeClient(client: Client) {
        connectedClients.remove(client)
    }
    fun serverDisconnection() {
        connected = false
        socket = null
        changeBottomBarStateTo(false)
    }
    fun notConnected(): Boolean = !connected
    fun cleanServerSocket() {
        serverSocket?.close()
        serverSocket = null
    }
    fun cleanSocket() {
        socket?.close()
        socket = null
    }
}