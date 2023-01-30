package com.example.localnetworkingandroidapp.model

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.example.localnetworkingandroidapp.data.LinkStates
import com.example.localnetworkingandroidapp.model.WifiConnectionState.socket
import com.example.localnetworkingandroidapp.model.WifiConnectionState.writer
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket
import java.net.UnknownHostException

class Listeners(connectionVM: ConnectionViewModel) {
    fun getResolveListener(): NsdManager.ResolveListener = object : NsdManager.ResolveListener {

        val TAG = "resolveListener"

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.i(TAG, "Resolve Succeeded. $serviceInfo")

            socket?.let {
                Log.i(TAG, "Socket already connected $it")
                return
            }

            try {
                // Connect to the host
                socket = Socket(serviceInfo.host, serviceInfo.port)
                writer = PrintWriter(socket!!.getOutputStream())
                WifiConnectionState.updateLinkStateTo(LinkStates.Connected)

                WifiConnectionState.changeBottomBarStateTo(true)
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Unknown host. ${e.localizedMessage}")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to create writer. ${e.localizedMessage}")
            }
        }
    }

    var discoveryListener = getADiscoveryListener()

    private fun getADiscoveryListener(): NsdManager.DiscoveryListener = object : NsdManager.DiscoveryListener {

        val TAG = "discoveryListener"

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service found ${service.serviceName}")

            // A service was found! Do something with it.
            if (service.serviceName == Names.NetworkSearchDiscoveryName) {
                WifiConnectionState.nsdManager.resolveService(service, getResolveListener())
//                WifiConnectionState.nsdManager.resolveService(service, mResolveListener)
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
            Log.d("NsdManager.Registration", "Service registered as \"${NsdServiceInfo.serviceName}\"")
            if (NsdServiceInfo.serviceName != Names.NetworkSearchDiscoveryName) {
                connectionVM.restart()
            }
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