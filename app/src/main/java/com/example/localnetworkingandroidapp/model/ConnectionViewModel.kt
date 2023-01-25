package com.example.localnetworkingandroidapp.model

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localnetworkingandroidapp.data.channel.ChannelService
import com.example.localnetworkingandroidapp.data.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.SocketException

class ConnectionViewModel(
    val listeners: Listeners,
): ViewModel() {
    private val TAG = "ConnectionVM"
    private lateinit var serviceInfo: NsdServiceInfo
//    var channel: ChannelService? = null

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "start")
            // Browse for existing services
            startSearching()
            delay(3000L)
            if (WifiConnectionState.notConnected()) {
                Log.i(TAG, "connection not found")
                startHosting()
            }
        }
    }
    private suspend fun startSearching() {
//        WifiConnectionState.nsdManager.discoverServices("_LocalNetworkingApp._tcp", NsdManager.PROTOCOL_DNS_SD, listeners.discoveryListener)
        WifiConnectionState.nsdManager.discoverServices("_LocalNetworkingApp._tcp", NsdManager.PROTOCOL_DNS_SD, listeners.getDiscoveryListener())
    }
    private suspend fun startHosting() {
        Log.i(TAG, "startHosting")

        createListenSocket()
        // Create a listen socket
//        var localPort: Int
//        WifiConnectionState.serverSocket = ServerSocket(0).also { socket ->
//             Store the chosen port.
//            localPort = socket.localPort
//        }
//        Log.i(TAG, "server socket localPort $localPort")
//        registerService(localPort)
        WifiConnectionState.serverSocket?.let { registerService(it.localPort) }
//        registerService(WifiConnectionState.serverSocket.localPort)
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "loop on isHosting")
            while (WifiConnectionState.isHosting()) {
                try {
                    WifiConnectionState.serverSocket?.accept()?.let {
                        WifiConnectionState.names.findAName()
                        Log.d("ServerSocket", "accepted client")
                        // Give the client their name
                        val writer: PrintWriter

                        try { writer = PrintWriter(it.getOutputStream()) }
                        catch (e: IOException) {
                            Log.w("ServerSocket", "Failed to create writer for $it")
                            return@let
                        }

                        val client = Client(it, WifiConnectionState.names.myName, writer)
                        WifiConnectionState.connectedClients.add(client)

                        // Start reading messages
//                        Thread(ClientReader(client)).start()
                        WifiConnectionState.channelToClient ?: let {
                            WifiConnectionState.channelToServer = ChannelService.createChannelToClient(client)
                        }
                        WifiConnectionState.channelToClient?.open()
                    }
                } catch (e: SocketException) {
                    break
                }
            }

            // Create the NsdServiceInfo object, and populate it.
//            val serviceInfo = NsdServiceInfo().apply {
//                // The name is subject to change based on conflicts
//                // with other services advertised on the same network.
//                serviceName = "BelgariadChat"
//                serviceType = "_LocalNetworkingApp._tcp"
////            setPort(port)
//                setPort(localPort)
////                setPort(WifiConnectionState.serverSocket)
//            }
        }

        // Reset the text view
//        main_activity_textview.text = ""

//         Add a system message
//        val message = Message(Message.SERVER_MSG_SENDER, "$myName has started the chat", Date().time)
//        addMessage(message, false)

        // Enable chat
        WifiConnectionState.changeBottomBarStateTo(true)

        // Initialize cannonical thread
//        cannonicalThread = Collections.synchronizedList(ArrayList<Message>()).apply {
//            add(message)
//        }
    }

    private fun createListenSocket() {
        Log.i(TAG, "createListenSocket")
        var localPort: Int
        WifiConnectionState.serverSocket = ServerSocket(0).also { socket ->
            // Store the chosen port.
            localPort = socket.localPort
        }
        Log.i(TAG, "server socket localPort $localPort")
    }

    private fun registerService(port: Int) {
        Log.i(TAG, "registerService port $port")
        // Create the NsdServiceInfo object, and populate it.
        serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = "BelgariadChat"
            serviceType = "_LocalNetworkingApp._tcp"
            setPort(port)
        }
        // Register the service for discovery
        WifiConnectionState.nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listeners.registrationListener)
    }
}