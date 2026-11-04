package com.example.projecte1_repo_app

object CestaManager {
    // Mapa que guarda l'entrada i la quantitat seleccionada
    private val elementsCesta = mutableMapOf<TipusEntrada, Int>()

    fun afegirEntrada(entrada: TipusEntrada) {
        // Només permetre si és general (per si de cas s'escapa algun clic)
        if (entrada.tipusZona == "general") {
            val quantitatActual = elementsCesta[entrada] ?: 0
            elementsCesta[entrada] = quantitatActual + 1
        }
    }

    // Retorna el nombre total d'elements acumulats a la cistella
    fun obtenirTotalElements(): Int {
        return elementsCesta.values.sum()
    }

    // Retorna tot el contingut (ens servirà moltíssim a la Tasca 14)
    fun obtenirElements(): Map<TipusEntrada, Int> {
        return elementsCesta
    }
    fun buidar() {
        elementsCesta.clear()
    }
}