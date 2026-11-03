package com.example.projecte1_repo_app

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetallEsdevenimentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detall_esdeveniment)

        // 1. Definim els elements de la pantalla (el botó d'afegir i el text del comptador)
        val btnAfegir: Button = findViewById(R.id.btnAfegir)
        val txtComptador: TextView = findViewById(R.id.txtComptadorCesta)

        // Creem un exemple d'entrada general per provar-ho
        val entradaDeProva = TipusEntrada("T1", "Entrada General Dempeus", 12.00, "general")

        // 2. Programem què passa quan l'usuari CLICA el botó
        btnAfegir.setOnClickListener {

            // Pas A: Guardem l'entrada al nostre CestaManager
            CestaManager.afegirEntrada(entradaDeProva)

            // Pas B: Preguntem al CestaManager quin és el total d'elements ara mateix
            val totalActualitzat = CestaManager.obtenirTotalElements()

            // Pas C: Pintem aquest número al TextView del comptador de la pantalla
            txtComptador.text = totalActualitzat.toString()

            // Opcional: Un petit avís visual perquè l'usuari ho vegi clar
            Toast.makeText(this, "Entrada afegida a la cistella!", Toast.LENGTH_SHORT).show()
        }
    }
}