package com.example.projecte1_repo_app

data class TipusEntrada(
    val idTipus: String,
    val nomTipus: String,
    val preuTipus: Double,
    val tipusZona: String = "general"
)