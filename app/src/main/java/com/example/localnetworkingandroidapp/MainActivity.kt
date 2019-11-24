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
import android.text.style.AlignmentSpan
import android.view.MenuItem
import android.view.Menu
import android.widget.TextView
import java.net.ServerSocket
import java.net.Socket
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import kotlin.random.Random

const val MESSAGE_TERMINATOR = "\r\n"

class MainActivity : AppCompatActivity(), NsdManager.RegistrationListener {

    var joined = false
    var connected = false

    var serverSocket: ServerSocket? = null
    var nsdManager: NsdManager? = null
    var connectedSockets = ArrayList<Socket>()

    var clientNames = HashMap<Socket, String>()
    var names = mutableListOf(
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

                // Reset the chat
                findViewById<TextView>(R.id.main_activity_textview).text = ""
            } else {
                // Update the join/leave title
                item.title = getString(R.string.leave)

                // Show a connecting message
                val string = getString(R.string.connection_server_message)
                val spannable = SpannableString(string)
                spannable.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                findViewById<TextView>(R.id.main_activity_textview).text = spannable

                // Browse for existing services
                startSearching()

                // After 5 seconds, if no service has been found, start one
                Handler().postDelayed({
                    if (!connected) {
                        // TODO: Stop joining
                        startHosting()
                    }
                }, 5000)
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

    private fun startSearching() {

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
                serverSocket?.accept()?.let {
                    runOnUiThread {
                        connectedSockets.add(it)
                    }

                    // Give the client their name
                    val name = getName()
                    clientNames[it] = name
                    Thread(ClientWriter(it, name)).start()

                    // Start reading messages
                    Thread(ClientReader(it)).start()
                }
            }
        }).start()

        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = "Belgariad Chat"
            serviceType = "_LocalNetworkingApp._tcp"
            setPort(port)
        }

        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).also {
            it.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, this)
        }
    }

    inner class ClientReader(private val client: Socket): Runnable {
        override fun run() {
            var line: String
            val reader: BufferedReader

            try {
                reader = BufferedReader(InputStreamReader(client.getInputStream()))
            } catch (e: IOException) {
                println("in or out failed")

                // TODO: Inform the server of the failure
                return
            }

            while (true) {
                try {
                    line = reader.readLine()

                    if (line == null) {
                        runOnUiThread {
                            removeClient(client)
                        }
                        break
                    }

                    // TODO: Pass the message back to the activity
                } catch (e: IOException) {
                    runOnUiThread {
                        removeClient(client)
                    }
                    return
                }

            }
        }
    }

    class ClientWriter(private val client: Socket, private val message: String): Runnable {
        override fun run() {
            val writer: PrintWriter

            try {
                writer = PrintWriter(client.getOutputStream(), true)
            } catch (e: IOException) {

                // TODO: Inform the server of the failure
                println("Write failed")
                return
            }

            writer.print(message + MESSAGE_TERMINATOR)
        }
    }

    fun removeClient(client: Socket) {
        connectedSockets.remove(client)
        clientNames.remove(client)?.let {
            putName(it)

            // TODO: Send a system message alerting that a client has left
        }
    }

    // NsdManager.Registration listener methods
    override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
        // Save the service name. Android may have changed it in order to
        // resolve a conflict, so update the name you initially requested
        // with the name Android actually used.
    }

    override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        // Registration failed! Put debugging code here to determine why.
    }

    override fun onServiceUnregistered(arg0: NsdServiceInfo) {
        // Service has been unregistered. This only happens when you call
        // NsdManager.unregisterService() and pass in this listener.
    }

    override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
        // Unregistration failed. Put debugging code here to determine why.
    }

    fun getName(): String {
        return names.removeAt(Random.nextInt(names.size))
    }

    fun putName(name: String) {
        names.add(name)
    }
}
