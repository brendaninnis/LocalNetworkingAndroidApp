package com.example.localnetworkingandroidapp.model

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localnetworkingandroidapp.data.channel.ChannelService
import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.data.Message.Companion.MESSAGE_TERMINATOR
import com.example.localnetworkingandroidapp.model.WifiConnectionState.changeBottomBarStateTo
import com.example.localnetworkingandroidapp.model.WifiConnectionState.channelToServer
import com.example.localnetworkingandroidapp.model.WifiConnectionState.connectedClients
import com.example.localnetworkingandroidapp.model.WifiConnectionState.nsdManager
import kotlinx.coroutines.*
import java.io.IOException
import java.io.PrintWriter
import java.lang.IllegalArgumentException
import java.net.ServerSocket
import java.net.SocketException
import java.util.*

class ConnectionViewModel(
    private val listeners: Listeners,
    private val canonicalThread: CanonicalThread,
): ViewModel() {
    private val TAG = "ConnectionVM"
    private lateinit var serviceInfo: NsdServiceInfo
    var serverSocket: ServerSocket? = null
    fun isHosting(): Boolean = serverSocket?.let { true } ?: false

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "start")
            // Browse for existing services
            startSearching()
            delay(3000L)
            stopSearching()
            if (WifiConnectionState.notConnected()) {
                Log.i(TAG, "connection not found")
                startHosting()
            } else {
                startListenServer()
            }
        }
    }

    private suspend fun startListenServer() {
        Log.e(TAG, "startReading")
        channelToServer = ChannelService.createChannelToServer(WifiConnectionState.socket!!, canonicalThread)
        channelToServer?.open()
    }

    private fun startSearching() {
        nsdManager.discoverServices(
            "_LocalNetworkingApp._tcp",
            NsdManager.PROTOCOL_DNS_SD,
            listeners.discoveryListener
//            listeners.getADiscoveryListener()
        )
    }

    private suspend fun startHosting() {
//        Names.deviceName = Names.getNewName()
        Log.e(TAG, "startHosting as ${Names.deviceName}")

        // Create a listen socket
        createListenSocket()
        serverSocket?.let { _serverSocket ->
            registerService(_serverSocket.localPort)

            val hostName = Names.getNewName()
            Names.deviceName = hostName
            val startMessage = Message(Message.SERVER_MSG_SENDER, "$hostName has started the chat", Date().time)
            canonicalThread.addMessage(startMessage)

            //Listen for new Client
            viewModelScope.launch(Dispatchers.IO) {
                Log.i(TAG, "loop on isHosting")
                while ( isHosting() ) {
                    try {
                        _serverSocket.accept()?.let {
                            // Give the client their name
                            Log.w(TAG, "accept new client ${it.inetAddress}")
                            val name = Names.getNewName()
                            val writer: PrintWriter

                            try { writer = PrintWriter(it.getOutputStream()) }
                            catch (e: IOException) {
                                Log.w(TAG, "Failed to create writer for $it")
                                return@let
                            }

                            val newClient = Client(it, name, writer)
                            Log.e(TAG, "accepted client ${newClient.name}${newClient.socket.inetAddress}")
                            connectedClients.add(newClient)

                            // Send a system message attributing client device name
                            val nameMessage = Message(Message.SERVER_NAME_SENDER, name, Date().time)
                            writer.print(nameMessage.toJson() + Message.MESSAGE_TERMINATOR)
                            writer.flush()
                            // Send a system message alerting that a client has joined

                            async {

                                val joiningMessage =
                                    Message(Message.SERVER_MSG_SENDER, "$name has joined", Date().time)
                                connectedClients.forEach {
                                    if (it != newClient) {
                                        it.writer.print(joiningMessage.toJson() + Message.MESSAGE_TERMINATOR)
                                        it.writer.flush()
                                    }
                                }
                                canonicalThread.addMessage(joiningMessage)
//                            addMessage(joiningMessage)
//                            sendMessageToClients(joiningMessage)

                                // Send the client the cannonical thread
                                var messages = ""
                                canonicalThread.messageList.value.forEach {
                                    messages += it.toJson() + MESSAGE_TERMINATOR
                                }
                                writer.print(messages)
                                writer.flush()
                            }

                            // Start reading messages
                            val aside = async { ChannelService.createChannelToClient(newClient, connectedClients, canonicalThread).open() }
                        }
                    } catch (e: SocketException) {
                        break
                    }
                }
            }

            // Enable chat
            changeBottomBarStateTo(true)
        }
    }


    private fun createListenSocket() {
        Log.i(TAG, "createListenSocket")
        var localPort: Int
        serverSocket = ServerSocket(0).also { socket ->
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
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listeners.registrationListener)
    }

    fun stopSearching() {
        try {
//            nsdManager.stopServiceDiscovery(listeners.getADiscoveryListener())
            nsdManager.stopServiceDiscovery(listeners.discoveryListener)
        } catch (e: IllegalArgumentException) {
            Log.i("nsdManager", "discoveryListener not registered")
        }
    }
    fun stopHosting() {
        // Stop broadcasting service
        nsdManager.unregisterService(listeners.registrationListener)

        // Remove the clients
        connectedClients.forEach {
            it.writer.close()
            it.socket.close()
        }

        // Reset Names
        Names.reset()

        // Disable chat
        changeBottomBarStateTo(false)

        // Reset Messages
        canonicalThread.reset()

        // Stop Hosting
        serverSocket?.close()
        serverSocket = null
    }
}