package com.example.projecte1_repo_app

import com.google.gson.annotations.SerializedName
data class Esdeveniment(
    val id: String,
    val nom: String,
    val tipusEntrades: List<TipusEntrada>,
    var isExpanded: Boolean = false,
    @SerializedName("dataHora") val data: String
)