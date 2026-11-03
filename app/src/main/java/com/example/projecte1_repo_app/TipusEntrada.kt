package com.example.projecte1_repo_app

data class TipusEntrada(
    val id: String,
    val nom: String,
    val preu: Double,
    val tipusZona: String // "general" o "numerada"
)