package com.example.projecte1_repo_app

data class ButacaCesta(
    val idEsdeveniment: String,
    val idTipus: String,
    val nomTipus: String,
    val preu: Double,
    val fila: Int,
    val numero: Int,
    val ambError: Boolean = false
)
object CestaManager {
    // Mapa que guarda l'entrada i la quantitat seleccionada
    private val elementsCesta = mutableMapOf<TipusEntrada, Int>()

    //lista de butacas numeradas
    private val butaques = mutableListOf<ButacaCesta>()
    fun afegirEntrada(entrada: TipusEntrada) {
        // Només permetre si és general (per si de cas s'escapa algun clic)
        if (entrada.tipusZona == "general") {
            val quantitatActual = elementsCesta[entrada] ?: 0
            elementsCesta[entrada] = quantitatActual + 1
        }
    }

    // Retorna el nombre total d'elements acumulats a la cistella
    //suma tambe las butacas
    fun obtenirTotalElements(): Int {
        return elementsCesta.values.sum() + butaques.size
    }

    // Retorna tot el contingut (ens servirà moltíssim a la Tasca 14)
    fun obtenirElements(): Map<TipusEntrada, Int> {
        return elementsCesta
    }
    //vaciar tambien butacas
    fun buidar() {
        elementsCesta.clear()
        butaques.clear()
    }

    // funcionamiento botones +/-
    // cantidad de entradas generales
    fun canviarQuantitat(entrada: TipusEntrada, delta: Int) {
        val actual = elementsCesta[entrada] ?: return
        val nova = actual + delta
        if (nova >= 1) elementsCesta[entrada] = nova
    }

    fun eliminarEntrada(entrada: TipusEntrada) {
        elementsCesta.remove(entrada)
    }

    //butacas numeradas
    fun obtenirButaques(): List<ButacaCesta> = butaques.toList()

    fun esButacaSeleccionada(idEsdeveniment: String, fila: Int,numero: Int): Boolean =
        butaques.any { it.idEsdeveniment == idEsdeveniment && it.fila == fila && it.numero == numero
        }
    fun alternarButaca(b: ButacaCesta): Boolean {
        val treta = butaques.removeAll {
            it.idEsdeveniment == b.idEsdeveniment && it.fila == b.fila && it.numero == b.numero
        }
        if (!treta) butaques.add(b)
        return !treta
    }
    fun eliminarButaca(b: ButacaCesta){
        butaques.removeAll {
            it.idEsdeveniment == b.idEsdeveniment && it.fila == b.fila && it.numero == b.numero
        }
    }

}