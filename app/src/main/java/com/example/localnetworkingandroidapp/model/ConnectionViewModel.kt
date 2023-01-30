package com.example.localnetworkingandroidapp.model

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localnetworkingandroidapp.data.channel.ChannelService
import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.data.LinkStates
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.data.Message.Companion.MESSAGE_TERMINATOR
import com.example.localnetworkingandroidapp.model.WifiConnectionState.changeBottomBarStateTo
import com.example.localnetworkingandroidapp.model.WifiConnectionState.connectedClients
import com.example.localnetworkingandroidapp.model.WifiConnectionState.linkState
import com.example.localnetworkingandroidapp.model.WifiConnectionState.nsdManager
import com.example.localnetworkingandroidapp.model.WifiConnectionState.updateLinkStateTo
import kotlinx.coroutines.*
import java.io.IOException
import java.io.PrintWriter
import java.lang.IllegalArgumentException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.*

class ConnectionViewModel(
    private val canonicalThread: CanonicalThread,
): ViewModel() {
    private val TAG = "ConnectionVM"
    private lateinit var serviceInfo: NsdServiceInfo
    private var serverSocket: ServerSocket? = null
    private val listeners = Listeners(this)
    fun isHosting(): Boolean = serverSocket?.let { true } ?: false

    fun start() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "start")
            updateLinkStateTo(LinkStates.Connecting)
            // Browse for existing services
            startSearching()
            delayUntilConnected()
            stopSearching()
            when (linkState.value) {
                LinkStates.Connected -> startListenServer()
                LinkStates.Connecting -> startHosting()
                LinkStates.NotConnected -> Log.e(TAG, "ERROR")
            }
        }
    }

    private suspend fun delayUntilConnected() {
        var count = 0
        while (linkState.value == LinkStates.Connecting && count < 8) {
            delay(500L)
            count += 1
        }
    }


    private fun startSearching() {
        nsdManager.discoverServices(
            "_LocalNetworkingApp._tcp",
            NsdManager.PROTOCOL_DNS_SD,
            listeners.discoveryListener
        )
    }

    private suspend fun startListenServer() {
        Log.e(TAG, "start listen server ")
        ChannelService.createChannelToServer(WifiConnectionState.socket!!, canonicalThread).open()
    }

    private suspend fun startHosting() {

        // Create a listen socket
        initializeServerSocket()
        serverSocket?.let { _serverSocket ->
            registerService(_serverSocket.localPort)
            updateLinkStateTo(LinkStates.Connected)

            val hostName = Names.getNewName()
            Names.deviceName = hostName
            val startMessage = Message(Message.SERVER_MSG_SENDER, "$hostName has started the chat", Date().time)
            Log.e(TAG, "startHosting as ${Names.deviceName}")
            canonicalThread.addMessage(startMessage)

            //Listen for new Client
            viewModelScope.launch(Dispatchers.IO) {
                Log.i(TAG, "loop on isHosting")
                while ( isHosting() ) {
                    try {
                        _serverSocket.accept()?.let {
                            Log.w(TAG, "accept new client ${it.inetAddress}")
                            // Give the client their name
                            registerNewClient(it) ?: let { return@let }
                            val newClient = connectedClients.last()

                            // Send a system message attributing client device name
                            val nameMessage = Message(Message.SERVER_NAME_SENDER, newClient.name, Date().time)
                            SendMessage(nameMessage).toClient(newClient)


                            // Send a system message alerting that a client has joined
                            val joiningMessage =
                                Message(Message.SERVER_MSG_SENDER, "${newClient.name} has joined", Date().time)

                            canonicalThread.addMessage(joiningMessage)
                            SendMessage(joiningMessage, canonicalThread).toAllClients(exception = newClient)

                            // Send the client the cannonical thread
                            var messages = ""
                            canonicalThread.messageList.value.forEach {
                                messages += it.toJson() + MESSAGE_TERMINATOR
                            }
                            newClient.writer.print(messages)
                            newClient.writer.flush()


                            // Start reading messages
                            val aside = async {
                                ChannelService.createChannelToClient(
                                    newClient,
                                    canonicalThread
                                ).open()
                            }
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
    private fun registerNewClient(socket: Socket): Unit? {
        val name = Names.getNewName()
        val writer: PrintWriter

        try { writer = PrintWriter(socket.getOutputStream()) }
        catch (e: IOException) {
            Log.w(TAG, "Failed to create writer for $socket")
            return null
        }

        val newClient = Client(socket, name, writer)
        Log.e(TAG, "accepted client ${newClient.name}${newClient.socket.inetAddress}")
        connectedClients.add(newClient)
        return Unit
    }


    private fun initializeServerSocket() {
        Log.i(TAG, "createListenSocket")
        var localPort: Int
        // Initialize a server socket on the next available port.
        serverSocket = ServerSocket(0).also { socket ->
            // Store the chosen port.
            localPort = socket.localPort
        }
        Log.i(TAG, "server socket localPort : $localPort")
    }

    private fun registerService(port: Int) {
        // Create the NsdServiceInfo object, and populate it.
        serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = Names.NetworkSearchDiscoveryName
            serviceType = "_LocalNetworkingApp._tcp"
            setPort(port)
        }
        // Register the service for discovery
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listeners.registrationListener)
        Log.d(TAG, "register service ${serviceInfo.serviceName} ${serviceInfo.port} ${serviceInfo.host} ${serviceInfo.serviceType}")
    }

    fun stopSearching() {
        try {
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
        connectedClients = mutableListOf()

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

    fun restart() {
        viewModelScope.launch {
            stopHosting()
            start()
        }
    }
}