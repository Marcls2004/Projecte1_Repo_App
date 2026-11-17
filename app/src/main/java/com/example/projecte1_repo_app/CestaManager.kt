package com.example.projecte1_repo_app

object CestaManager {
    // Mapa que guarda l'entrada i la quantitat seleccionada
    private val elementsCesta = mutableMapOf<TipusEntrada, Int>()

    // Spec 22: llista de les butaques numerades que l'usuari ha seleccionat
    private val seientsCesta = mutableListOf<SeientCistella>()

    fun afegirEntrada(entrada: TipusEntrada) {
        // Només permetre si és general (per si de cas s'escapa algun clic)
        if (entrada.tipusZona == "general") {
            val quantitatActual = elementsCesta[entrada] ?: 0
            elementsCesta[entrada] = quantitatActual + 1
        }
    }

    // Spec 22: posa una butaca a la cistella (si ja hi és, no la torna a posar)
    fun afegirSeient(seient: SeientCistella) {
        if (!esSeleccionat(seient.idEsdeveniment, seient.idTipus, seient.fila, seient.seient)) {
            seientsCesta.add(seient)
        }
    }

    // Spec 22: treu de la cistella la butaca d'aquest esdeveniment, tipus, fila i seient
    fun treureSeient(idEsdeveniment: String, idTipus: String, fila: Int, seient: Int) {
        for (s in seientsCesta) {
            if (s.idEsdeveniment == idEsdeveniment && s.idTipus == idTipus &&
                s.fila == fila && s.seient == seient) {
                seientsCesta.remove(s)
                return
            }
        }
    }

    // Spec 22: retorna true si aquesta butaca ja és a la cistella (per pintar-la de color seleccionat)
    fun esSeleccionat(idEsdeveniment: String, idTipus: String, fila: Int, seient: Int): Boolean {
        for (s in seientsCesta) {
            if (s.idEsdeveniment == idEsdeveniment && s.idTipus == idTipus &&
                s.fila == fila && s.seient == seient) {
                return true
            }
        }
        return false
    }

    // Spec 22: retorna totes les butaques de la cistella (per mostrar-les i comprar-les)
    fun obtenirSeients(): List<SeientCistella> {
        return seientsCesta
    }

    // Retorna el nombre total d'elements acumulats a la cistella
    fun obtenirTotalElements(): Int {
        return elementsCesta.values.sum() + seientsCesta.size
    }

    // Retorna tot el contingut (ens servirà moltíssim a la Tasca 14)
    fun obtenirElements(): Map<TipusEntrada, Int> {
        return elementsCesta
    }
    fun buidar() {
        elementsCesta.clear()
        seientsCesta.clear()
    }
}