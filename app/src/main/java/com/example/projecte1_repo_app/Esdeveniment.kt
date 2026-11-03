package com.example.projecte1_repo_app

data class Esdeveniment(
    val id: Int,
    val nom: String,
    val data: String,
    val lloc: String,
    val tipusEntrades: List<TipusEntrada>,
    var isExpanded: Boolean = false // MP08: Controla si la celda está desplegada
)