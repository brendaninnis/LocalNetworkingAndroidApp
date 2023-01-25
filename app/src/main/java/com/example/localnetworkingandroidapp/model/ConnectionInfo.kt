package com.example.localnetworkingandroidapp.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionInfo {
    private val _bottomBarEnable = MutableStateFlow(false)
    val bottomBarEnable: StateFlow<Boolean> = _bottomBarEnable.asStateFlow()
    fun changeBottomBarStateTo(newState: Boolean) {_bottomBarEnable.value = newState}

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
}