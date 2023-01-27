package com.example.localnetworkingandroidapp.model

object Names {
    val originalListNames = listOf(
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
    var availableNames: MutableList<String> = originalListNames.toMutableList()
    var deviceName: String = ""
    fun getNewName(): String {
        val newName = availableNames.random()
        availableNames.remove(newName)
        return newName
    }
    fun reset() {
        deviceName = ""
        availableNames = originalListNames.toMutableList()
    }
}