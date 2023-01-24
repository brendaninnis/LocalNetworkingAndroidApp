package com.example.localnetworkingandroidapp.ui.screen

sealed class ConnectionButtonState(val string: String) {
    object Join: ConnectionButtonState("JOIN")
    object Leave: ConnectionButtonState("LEAVE")
}
