package com.example.projecte1_repo_app

// Spec 22: una butaca numerada que l'usuari ha posat a la cistella
data class SeientCistella(
    val idEsdeveniment: String,   // codi de l'esdeveniment (p. ex. "E1")
    val idTipus: String,          // codi del tipus d'entrada numerada (p. ex. "T1_CEN")
    val nomTipus: String,         // nom per mostrar (p. ex. "Platea Central")
    val preu: Double,             // preu d'aquesta butaca
    val fila: Int,                // número de fila dins la zona (comença per 1)
    val seient: Int               // número de butaca dins la fila (comença per 1)
)