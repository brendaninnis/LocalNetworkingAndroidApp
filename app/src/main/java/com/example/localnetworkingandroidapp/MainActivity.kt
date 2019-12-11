package com.example.localnetworkingandroidapp

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MenuItem
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.net.ServerSocket
import java.net.Socket
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.lang.IllegalArgumentException
import java.net.SocketException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.collections.ArrayList
import kotlin.random.Random

const val MESSAGE_TERMINATOR = "\r\n"

class MainActivity : AppCompatActivity() {

    data class Client(val socket: Socket, val name: String, val writer: PrintWriter)

    var joined = false
    var connected = false
    var hostAfterHandler: Handler? = null
    var host = false

    var serverSocket: ServerSocket? = null
    var socket: Socket? = null
    var writer: PrintWriter? = null
    val nsdManager: NsdManager by lazy {
        (getSystemService(Context.NSD_SERVICE) as NsdManager)
    }
    var connectedClients: MutableList<Client> = CopyOnWriteArrayList<Client>()

    var cannonicalThread: MutableList<Message> = Collections.synchronizedList(ArrayList<Message>())

    private var names = mutableListOf(
        "Belgarion",
        "Ce'Nedra",
        "Belgarath",
        "Polgara",
        "Durnik",
        "Silk",
        "Velvet",
        "Poledra",
        "Beldaran",
        "Beldin",
        "Geran",
        "Mandorallen",
        "Hettar",
        "Adara",
        "Barak"
    )
    var myName = ""

    private val resolveListener = object : NsdManager.ResolveListener {

        val TAG = "resolveListener"

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")

            socket?.let {
                Log.i(TAG, "Socket already connected $it")
                return
            }

            try {
                // Connect to the host
                socket = Socket(serviceInfo.host, serviceInfo.port)
                writer = PrintWriter(socket!!.getOutputStream())

                runOnUiThread {
                    // Reset the chat
                    main_activity_textview.text = ""

                    // Enable chat
                    main_activity_send_button.isEnabled = true
                    main_activity_edit_text.isEnabled = true
                }

                // Start reading messages
                Thread(ServerReader(socket!!)).start()

                connected = true
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Unknown host. ${e.localizedMessage}")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to create writer. ${e.localizedMessage}")
            }
        }
    }

    private val discoveryListener = object : NsdManager.DiscoveryListener {

        val TAG = "discoveryListener"

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service found ${service.serviceName}")
            hostAfterHandler?.removeCallbacksAndMessages(null)

            // A service was found! Do something with it.
            if (service.serviceName.contains("BelgariadChat")) {
                nsdManager.resolveService(service, resolveListener)
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
            nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            nsdManager.stopServiceDiscovery(this)
        }
    }

    private val registrationListener = object : NsdManager.RegistrationListener {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_activity_send_button.setOnClickListener {
            val text = main_activity_edit_text.text.toString()
            if (text == "") return@setOnClickListener

            // Hide the keyboard
            (getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.
                hideSoftInputFromWindow(main_activity_edit_text.windowToken, 0)

            val message = Message(myName, text, Date().time)

            // Add the message to the text view
            addMessage(message, true)

            Log.d("JSON", message.toJson())

            // Send the message over the network
            Thread(Runnable {
                if (host) {
                    cannonicalThread.add(message)
                    connectedClients.forEach {
                        it.writer.print(message.toJson() + MESSAGE_TERMINATOR)
                        it.writer.flush()
                    }
                } else {
                    socket?.let {
                        writer?.print(message.toJson() + MESSAGE_TERMINATOR)
                        writer?.flush()
                    }
                }
            }).start()

            // Clear the edit text
            main_activity_edit_text.setText("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_join -> {
            if (joined) {
                // Update the join/leave title
                item.title = getString(R.string.join)

                if (host) stopHosting() else {
                    stopSearching()
                    hostAfterHandler?.removeCallbacksAndMessages(null)
                    socket?.close()
                    socket = null
                }

                // Reset the chat
                main_activity_textview.text = ""
            } else {
                // Update the join/leave title
                item.title = getString(R.string.leave)

                // Show a connecting message
                val string = getString(R.string.connection_server_message)
                val spannable = SpannableString(string)
                spannable.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                main_activity_textview.text = spannable

                // Browse for existing services
                startSearching()

                // After 3 seconds, if no service has been found, start one
                hostAfterHandler = Handler()
                hostAfterHandler?.postDelayed({
                    stopSearching()
                    startHosting()
                }, 3000)
            }
            joined = !joined
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    @Synchronized
    private fun getName(): String {
        return names.removeAt(Random.nextInt(names.size))
    }

    @Synchronized
    private fun putName(name: String) {
        names.add(name)
    }

    private fun addMessage(message: Message, fromSelf: Boolean) {
        val context = this
        val textView = main_activity_textview
        when (message.sender) {
            Message.SERVER_NAME_SENDER -> return
            Message.SERVER_MSG_SENDER -> {
                // Print server message
                val spannable = SpannableString(message.message).apply {
                    setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, message.message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textSecondary)), 0, message.message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(13, true), 0, message.message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                textView.append(spannable)
                textView.append("\n\n")
            }
            else -> {
                // Print message
                var spannable = SpannableString(message.sender).apply {
                    setSpan(AlignmentSpan.Standard(if (fromSelf) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL), 0, message.sender.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textSecondary)), 0, message.sender.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(13, true), 0, message.sender.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                textView.append(spannable)
                textView.append("\n")

                spannable = SpannableString(message.message).apply {
                    setSpan(AlignmentSpan.Standard(if (fromSelf) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL), 0, message.message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textPrimary)), 0, message.message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    setSpan(AbsoluteSizeSpan(18, true), 0, message.message.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                textView.append(spannable)
                textView.append("\n\n")
            }
        }

        // Scroll the messages to the bottom
        main_activity_scrollview.fullScroll(View.FOCUS_DOWN)
    }

    private fun startSearching() {
        nsdManager.discoverServices("_LocalNetworkingApp._tcp", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    private fun stopSearching() {
        try {
            nsdManager.stopServiceDiscovery(discoveryListener)
        } catch (e: IllegalArgumentException) {
            Log.i("nsdManager", "discoveryListener not registered")
        }
    }

    private fun startHosting() {
        // Create a listen socket
        val port: Int
        serverSocket = ServerSocket(0).also { socket ->
            // Store the chosen port.
            port = socket.localPort
        }

        Thread(Runnable {
            while (serverSocket != null) {
                try {
                    serverSocket?.accept()?.let {
                        Log.d("ServerSocket", "accepted client")

                        // Give the client their name
                        val name = getName()
                        val writer: PrintWriter

                        try {
                            writer = PrintWriter(it.getOutputStream())
                        } catch (e: IOException) {
                            Log.w ("ServerSocket", "Failed to create writer for $it")
                            return@let
                        }

                        val client = Client(it, name, writer)
                        connectedClients.add(client)

                        Thread(Runnable {
                            val nameMessage = Message(Message.SERVER_NAME_SENDER, name, Date().time)
                            writer.print(nameMessage.toJson() + MESSAGE_TERMINATOR)
                            writer.flush()

                            // Send a system message alerting that a client has joined
                            val message =
                                Message(Message.SERVER_MSG_SENDER, "$name has joined", Date().time)
                            cannonicalThread.add(message)

                            connectedClients.forEach {
                                if (it != client) {
                                    it.writer.print(message.toJson() + MESSAGE_TERMINATOR)
                                    it.writer.flush()
                                }
                            }

                            runOnUiThread {
                                addMessage(message, false)
                            }

                            // Send the client the cannonical thread
                            var messages = ""
                            synchronized(cannonicalThread) {
                                cannonicalThread.forEach {
                                    messages += it.toJson() + MESSAGE_TERMINATOR
                                }
                            }
                            writer.print(messages)
                            writer.flush()
                        }).start()

                        // Start reading messages
                        Thread(ClientReader(client)).start()
                    }
                } catch (e: SocketException) {
                    break
                }
            }
        }).start()

        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = "BelgariadChat"
            serviceType = "_LocalNetworkingApp._tcp"
            setPort(port)
        }

        // Register the service for discovery
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)

        // Host mode on
        host = true

        // Get a name for the host
        myName = getName()

        // Reset the text view
        main_activity_textview.text = ""

        // Add a system message
        val message = Message(Message.SERVER_MSG_SENDER, "$myName has started the chat", Date().time)
        addMessage(message, false)

        // Enable chat
        main_activity_send_button.isEnabled = true
        main_activity_edit_text.isEnabled = true

        // Initialize cannonical thread
        cannonicalThread = Collections.synchronizedList(ArrayList<Message>()).apply {
            add(message)
        }
    }

    private fun stopHosting() {
        // Stop listening
        serverSocket?.close()
        serverSocket = null

        // Stop broadcasting service
        nsdManager.unregisterService(registrationListener)

        // Remove the clients
        connectedClients.forEach {
            it.writer.close()
            it.socket.close()
        }

        // Remove my name
        putName(myName)
        myName = ""

        // Disable chat
        main_activity_send_button.isEnabled = false
        main_activity_edit_text.isEnabled = false

        // Reset
        cannonicalThread = Collections.synchronizedList(ArrayList<Message>())
        main_activity_textview.text = ""

        // Host mode off
        host = false
    }

    fun removeClient(client: Client) {
        connectedClients.remove(client)
        putName(client.name)

        // Send a system message alerting that a client has left
        val message = Message(Message.SERVER_MSG_SENDER, "${client.name} has left", Date().time)
        cannonicalThread.add(message)

        connectedClients.forEach {
            it.writer.print(message.toJson() + MESSAGE_TERMINATOR)
            it.writer.flush()
        }

        runOnUiThread {
            addMessage(message, false)
        }
    }

    inner class ServerReader(private val socket: Socket): Runnable {
        val TAG = "ServerReader"

        override fun run() {
            var line: String?
            val reader: BufferedReader

            try {
                reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            } catch (e: IOException) {
                println("in or out failed")

                serverDisconnected()
                return
            }

            while (true) {
                try {
                    line = reader.readLine()

                    if (line == null) {
                        serverDisconnected()
                        break
                    }

                    Log.d(TAG, "Read line $line")

                    val message = Message.fromJson(line)

                    if (message.sender == Message.SERVER_NAME_SENDER) {
                        myName = message.message
                        continue
                    }

                    runOnUiThread {
                        addMessage(message, false)
                    }
                } catch (e: IOException) {
                    serverDisconnected()
                    return
                }

            }
        }
    }

    inner class ClientReader(private val client: Client): Runnable {
        val TAG = "ClientReader"

        override fun run() {
            var line: String?
            val reader: BufferedReader

            try {
                reader = BufferedReader(InputStreamReader(client.socket.getInputStream()))
            } catch (e: IOException) {
                println("in or out failed")

                removeClient(client)
                return
            }

            while (true) {
                try {
                    line = reader.readLine()

                    if (line == null) {
                        removeClient(client)
                        break
                    }

                    Log.d(TAG, "Read line $line")

                    val message = Message.fromJson(line)

                    cannonicalThread.add(message)

                    runOnUiThread {
                        addMessage(message, false)
                    }

                    Thread(Runnable {
                        connectedClients.forEach {
                            if (it != client) { // Don't send the message to the client who sent it
                                it.writer.print(line)
                                it.writer.flush()
                            }
                        }
                    }).start()
                } catch (e: IOException) {
                    removeClient(client)
                    return
                }

            }
        }
    }

    fun serverDisconnected() {
        connected = false
        joined = false
        socket = null

        runOnUiThread {
            main_activity_edit_text.isEnabled = false
            main_activity_send_button.isEnabled = false
        }
    }
}
