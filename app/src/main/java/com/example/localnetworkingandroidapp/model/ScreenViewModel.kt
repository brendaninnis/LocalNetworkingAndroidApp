package com.example.localnetworkingandroidapp.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.WifiConnectionState.connected
import com.example.localnetworkingandroidapp.model.WifiConnectionState.socket
import com.example.localnetworkingandroidapp.ui.screen.ConnectionButtonState
import com.example.localnetworkingandroidapp.ui.screen.getScreenLayoutConstraints
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class ScreenViewModel() : ViewModel() {
    private val TAG = "ScreenVM"
    val constraints = getScreenLayoutConstraints()
    val listeners = Listeners()
    val canonicalThread = CanonicalThread()
    val connectionVM = ConnectionViewModel(listeners, canonicalThread)

    private val _textInput = MutableStateFlow("Say Something...")
    val textInput: StateFlow<String> = _textInput.asStateFlow()
    fun handleKeyboardInput(str: String) {
        _textInput.value += str
    }

    private val _connectionButton = MutableStateFlow(ConnectionButtonState.Join.string)
    val connectionButton: StateFlow<String> = _connectionButton.asStateFlow()
    fun connectionButtonSwitchText() {
        when (_connectionButton.value) {
            ConnectionButtonState.Leave.string -> _connectionButton.value =
                ConnectionButtonState.Join.string
            ConnectionButtonState.Join.string -> _connectionButton.value =
                ConnectionButtonState.Leave.string
        }
    }

    fun connectionButtonClick() {
        Log.e(TAG, "click on connection button")
        if (connected) {
            if (connectionVM.isHosting())
                connectionVM.stopHosting()
            else {
                connectionVM.stopSearching()
                socket?.close()
                socket = null
            }
        } else {
            connectionVM.start()
        }

        connectionButtonSwitchText()

//        if (isHosting() || connected) {
//            Log.i( TAG, "is Hosting ${isHosting()} connected $connected" )
//            Log.i(TAG, "stop discovery")
//            connectionVM.stopSearching()
//            WifiConnectionState.cleanServerSocket()
//            WifiConnectionState.cleanSocket()
//        } else
//            connectionVM.start()
    }

    fun handleSendButtonClick() {
        Log.e(TAG, "click on send button")
        viewModelScope.launch(Dispatchers.IO) {
            val message = Message(Names.deviceName, "first Message", Date().time)
            Log.i(TAG, "send from ${message.sender} : ${message.text}")
            if (connectionVM.isHosting()) {
                SendMessage(message, canonicalThread).toAllClients()
            } else {
                SendMessage(message, canonicalThread).toServer()
            }
        }
    }
}

