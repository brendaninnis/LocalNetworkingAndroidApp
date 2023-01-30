package com.example.localnetworkingandroidapp.model

import android.content.Context
import android.net.nsd.NsdManager
import android.util.Log
import com.example.localnetworkingandroidapp.data.channel.ChannelService
import com.example.localnetworkingandroidapp.data.Client
import com.example.localnetworkingandroidapp.data.LinkStates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.PrintWriter
import java.net.Socket

object WifiConnectionState {
    var socket: Socket? = null
    var connectedClients: MutableList<Client> = mutableListOf()
    lateinit var nsdManager: NsdManager
    var writer: PrintWriter? = null

    fun init(context: Context) {
        nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private val plinkState = MutableStateFlow(LinkStates.NotConnected)
    val linkState: StateFlow<LinkStates> = plinkState.asStateFlow()
    fun updateLinkStateTo(newState: LinkStates) {
        plinkState.value = newState
    }

    private val _bottomBarEnable = MutableStateFlow(false)
    val bottomBarEnable: StateFlow<Boolean> = _bottomBarEnable.asStateFlow()
    fun changeBottomBarStateTo(newState: Boolean) {_bottomBarEnable.value = newState}
}