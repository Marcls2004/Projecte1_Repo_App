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
    private val seientsCesta = mutableListOf<SeientCistella>()
    private val butaques = mutableListOf<ButacaCesta>()

    fun afegirEntrada(entrada: TipusEntrada) {
        if (entrada.tipusZona == "general") {
            val actual = elementsCesta[entrada] ?: 0
            elementsCesta[entrada] = actual + 1
        }
    }

    // Spec 22 (Miguel): añade asiento y lo sincroniza con la cesta de Anna
    fun afegirSeient(seient: SeientCistella) {
        if (!esSeleccionat(seient.idEsdeveniment, seient.idTipus, seient.fila, seient.seient)) {
            seientsCesta.add(seient)
            butaques.add(
                ButacaCesta(
                    idEsdeveniment = seient.idEsdeveniment,
                    idTipus = seient.idTipus,
                    nomTipus = seient.nomTipus,
                    preu = seient.preu,
                    fila = seient.fila,
                    numero = seient.seient
                )
            )
        }
    }

    fun treureSeient(idEsdeveniment: String, idTipus: String, fila: Int, seient: Int) {
        seientsCesta.removeAll {
            it.idEsdeveniment == idEsdeveniment && it.idTipus == idTipus &&
                    it.fila == fila && it.seient == seient
        }
        butaques.removeAll {
            it.idEsdeveniment == idEsdeveniment && it.idTipus == idTipus &&
                    it.fila == fila && it.numero == seient
        }
    }

    fun esSeleccionat(idEsdeveniment: String, idTipus: String, fila: Int, seient: Int): Boolean {
        return seientsCesta.any {
            it.idEsdeveniment == idEsdeveniment && it.idTipus == idTipus &&
                    it.fila == fila && it.seient == seient
        }
    }

    fun obtenirSeients(): List<SeientCistella> = seientsCesta

    fun obtenirTotalElements(): Int = elementsCesta.values.sum() + butaques.size

    fun obtenirElements(): Map<TipusEntrada, Int> = elementsCesta

    fun buidar() {
        elementsCesta.clear()
        seientsCesta.clear()
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

    // Gestió de butaques numerades (Spec 25 Anna)
    fun obtenirButaques(): List<ButacaCesta> = butaques.toList()

    fun esButacaSeleccionada(idEsdeveniment: String, fila: Int, numero: Int): Boolean =
        butaques.any { it.idEsdeveniment == idEsdeveniment && it.fila == fila && it.numero == numero }

    fun alternarButaca(b: ButacaCesta): Boolean {
        val treta = butaques.removeAll {
            it.idEsdeveniment == b.idEsdeveniment &&
                    it.idTipus == b.idTipus &&
                    it.fila == b.fila &&
                    it.numero == b.numero
        }
        if (!treta) {
            butaques.add(b)
            seientsCesta.add(SeientCistella(b.idEsdeveniment, b.idTipus, b.nomTipus, b.preu, b.fila, b.numero))
        } else {
            seientsCesta.removeAll {
                it.idEsdeveniment == b.idEsdeveniment && it.fila == b.fila && it.seient == b.numero
            }
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
        seientsCesta.removeAll {
            it.idEsdeveniment == b.idEsdeveniment && it.fila == b.fila && it.seient == b.numero
        }
    }

    // Spec 23 (Marc): marca i neteja errors en les butaques rebutjades
    fun marcarErrorsButaques(rebutjats: Set<String>) {
        for (i in butaques.indices) {
            val b = butaques[i]
            val clau = "${b.idEsdeveniment}|${b.idTipus}|${b.fila}|${b.numero}"
            butaques[i] = b.copy(ambError = rebutjats.contains(clau))
        }
    }

    fun netejarErrorsButaques() {
        for (i in butaques.indices) {
            if (butaques[i].ambError) {
                butaques[i] = butaques[i].copy(ambError = false)
            }
        }
    }
}
