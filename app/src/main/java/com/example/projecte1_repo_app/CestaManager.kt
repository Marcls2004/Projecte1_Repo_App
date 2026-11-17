
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

    private val elementsCesta = mutableMapOf<TipusEntrada, Int>()
    private val butaques = mutableListOf<ButacaCesta>()

    fun afegirEntrada(entrada: TipusEntrada) {
        if (entrada.tipusZona == "general") {
            val actual = elementsCesta[entrada] ?: 0
            elementsCesta[entrada] = actual + 1
        }
    }

    fun obtenirTotalElements(): Int {
        return elementsCesta.values.sum() + butaques.size
    }

    fun obtenirElements(): Map<TipusEntrada, Int> {
        return elementsCesta
    }

    fun buidar() {
        elementsCesta.clear()
        butaques.clear()
    }

    // Gestió de quantitats d'entrades generals

    fun canviarQuantitat(entrada: TipusEntrada, delta: Int) {
        val actual = elementsCesta[entrada] ?: return
        val nova = actual + delta

        if (nova >= 1) {
            elementsCesta[entrada] = nova
        }
    }

    fun eliminarEntrada(entrada: TipusEntrada) {
        elementsCesta.remove(entrada)
    }

    // Gestió de butaques numerades

    fun obtenirButaques(): List<ButacaCesta> {
        return butaques.toList()
    }

    fun esButacaSeleccionada(
        idEsdeveniment: String,
        fila: Int,
        numero: Int
    ): Boolean {
        return butaques.any {
            it.idEsdeveniment == idEsdeveniment &&
                    it.fila == fila &&
                    it.numero == numero
        }
    }

    fun alternarButaca(b: ButacaCesta): Boolean {
        val treta = butaques.removeAll {
            it.idEsdeveniment == b.idEsdeveniment &&
                    it.idTipus == b.idTipus &&
                    it.fila == b.fila &&
                    it.numero == b.numero
        }

        if (!treta) {
            butaques.add(b)
        }

        return !treta
    }

    fun eliminarButaca(b: ButacaCesta) {
        butaques.removeAll {
            it.idEsdeveniment == b.idEsdeveniment &&
                    it.idTipus == b.idTipus &&
                    it.fila == b.fila &&
                    it.numero == b.numero
        }
    }

    // Marcar les butaques rebutjades pel servidor

    fun marcarButacaError(
        idEsdeveniment: String,
        idTipus: String,
        fila: Int,
        numero: Int
    ) {
        val index = butaques.indexOfFirst {
            it.idEsdeveniment == idEsdeveniment &&
                    it.idTipus == idTipus &&
                    it.fila == fila &&
                    it.numero == numero
        }

        if (index != -1) {
            butaques[index] = butaques[index].copy(ambError = true)
        }
    }
}
