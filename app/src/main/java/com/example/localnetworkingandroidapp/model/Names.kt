package com.example.localnetworkingandroidapp.model

class Names {
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
    val availableNames: MutableList<String> = originalListNames.toMutableList()
    var myName: String = ""
    fun findAName() {
        if (myName.isEmpty()) { myName = availableNames.random() }
    }
}