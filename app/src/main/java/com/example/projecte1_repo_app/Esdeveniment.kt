package com.example.projecte1_repo_app

data class Esdeveniment(
    val id: String,
    val nom: String,
    val tipusEntrades: List<TipusEntrada>,
    var isExpanded: Boolean = false
)