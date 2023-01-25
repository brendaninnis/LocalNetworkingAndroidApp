package com.example.localnetworkingandroidapp.model

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.localnetworkingandroidapp.data.channel.ChannelService
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException

class Listeners() {
    fun getResolveListener(): NsdManager.ResolveListener = object : NsdManager.ResolveListener {

        val TAG = "resolveListener"

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve Succeeded. $serviceInfo")

            WifiConnectionState.socket?.let {
                Log.i(TAG, "Socket already connected $it")
                return
            }

            try {
                // Connect to the host
                WifiConnectionState.socket = Socket(serviceInfo.host, serviceInfo.port)
                WifiConnectionState.writer = PrintWriter(WifiConnectionState.socket!!.getOutputStream())
                WifiConnectionState.connected = true

                // Start reading messages
                WifiConnectionState.channelToServer = ChannelService.createChannelToServer(WifiConnectionState.socket!!)
//                WifiConnectionState.channelToClient.open()
//                Thread(ServerReader(WifiConnectionState.socket!!)).start()
                WifiConnectionState.connected = true
                Log.w(TAG, "Start reading message from Server")
                WifiConnectionState.changeBottomBarStateTo(true)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Unknown host. ${e.localizedMessage}")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to create writer. ${e.localizedMessage}")
            }
        }
    }
//    val discoveryListener = object : NsdManager.DiscoveryListener {
    fun getDiscoveryListener() = object : NsdManager.DiscoveryListener {

        val TAG = "discoveryListener"

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service found ${service.serviceName}")
//            hostAfterHandler?.removeCallbacksAndMessages(null)

            // A service was found! Do something with it.
            if (service.serviceName.contains("BelgariadChat")) {
                WifiConnectionState.nsdManager.resolveService(service, getResolveListener())
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            WifiConnectionState.nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            WifiConnectionState.nsdManager.stopServiceDiscovery(this)
        }
    }

    val registrationListener = object : NsdManager.RegistrationListener {
        override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            Log.d("NsdManager.Registration", "Service registered")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
            Log.d("NsdManager.Registration", "Registration failed")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            Log.d("NsdManager.Registration", "Service unregistered")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
            Log.d("NsdManager.Registration", "Unregistration failed")
        }
    }
}