package com.example.localnetworkingandroidapp.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localnetworkingandroidapp.data.LinkStates
import com.example.localnetworkingandroidapp.data.Message
import com.example.localnetworkingandroidapp.model.WifiConnectionState.linkState
import com.example.localnetworkingandroidapp.model.WifiConnectionState.socket
import com.example.localnetworkingandroidapp.model.WifiConnectionState.updateLinkStateTo
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
    val canonicalThread = CanonicalThread()
    private val connectionVM = ConnectionViewModel(canonicalThread)

    var textFieldTouched = false
    private val _textInput = MutableStateFlow("Say Something...")
    val textInput: StateFlow<String> = _textInput.asStateFlow()
    fun resetKeyboardInput(){
        _textInput.value = ""
    }
    fun handleKeyboardInput(input: String) {
        _textInput.value = input.trim()
    }

    fun connectionButtonClick() {
        when (linkState.value) {
            LinkStates.Connected -> {
                if (connectionVM.isHosting())
                    connectionVM.stopHosting()
                else {
                    connectionVM.stopSearching()
                    socket?.close()
                    socket = null
                    canonicalThread.reset()
                }
                updateLinkStateTo(LinkStates.NotConnected)
            }
            LinkStates.NotConnected -> {
                connectionVM.start()
            }
            LinkStates.Connecting -> Log.e(TAG, "ERROR linkState.value == LinkStates.Connecting")
        }
    }

    fun handleSendButtonClick() {
        viewModelScope.launch(Dispatchers.IO) {
            val message = Message(Names.deviceName, textInput.value, Date().time)
            resetKeyboardInput()
            Log.i(TAG, "send from ${message.sender} : ${message.text}")
            if (connectionVM.isHosting()) {
                SendMessage(message, canonicalThread).toAllClients()
            } else {
                SendMessage(message, canonicalThread).toServer()
            }
        }
    }
}

