package com.example.projecte1_repo_app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CestaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cesta)

        val listView: ListView = findViewById(R.id.listViewCesta)
        val txtTotalGlobal: TextView = findViewById(R.id.txtTotalGlobal)
        val btnValidar: Button = findViewById(R.id.btnValidarCesta)

        // 1. Recuperem els elements del CestaManager
        val elementsCesta = CestaManager.obtenirElements()

        val llistaTextos = mutableListOf<String>()
        var preuTotalGlobal = 0.0

        // 2. Recorrem el mapa per calcular el desglossament per línia i el total
        for ((entrada, quantitat) in elementsCesta) {
            val preuLinia = entrada.preu * quantitat
            preuTotalGlobal += preuLinia

            // Format que es veurà a la llista: Nom + Quantitat + Preu total de la línia
            llistaTextos.add("${entrada.nom} x$quantitat  ->  ${preuLinia} €")
        }

        // 3. Pintem la llista a la pantalla
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, llistaTextos)
        listView.adapter = adapter

        // 4. Mostrem el preu total global
        txtTotalGlobal.text = "Total a pagar: $preuTotalGlobal €"

        // 5. Botó de validar (preparat per a la Tasca 15)
        btnValidar.setOnClickListener {
            Toast.makeText(this, "Connectant amb el servidor per validar...", Toast.LENGTH_SHORT).show()            // Aquí afegiràs la petició WebSocket de la Tasca 15 amb en Miguel
        }
    }
}