package com.example.localnetworkingandroidapp.data

import java.io.PrintWriter
import java.net.Socket

data class Client(val socket: Socket, val name: String, val writer: PrintWriter)
