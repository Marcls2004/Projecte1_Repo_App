
package com.example.projecte1_repo_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetallEsdevenimentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detall_esdeveniment)

        val btnAfegir: Button = findViewById(R.id.btnAfegir)
        val txtComptador: TextView = findViewById(R.id.txtComptadorCesta)

        // Recuperamos la entrada seleccionada desde la cartelera
        val idTipus = intent.getStringExtra("idTipus")
        val nomTipus = intent.getStringExtra("nomTipus")
        val preuTipus = intent.getDoubleExtra("preuTipus", 0.0)
        val tipusZona = intent.getStringExtra("tipusZona") ?: "general"

        // Mostramos los datos de la entrada seleccionada
        val txtNomEntrada: TextView = findViewById(R.id.txtNomEntrada)
        val txtPreuEntrada: TextView = findViewById(R.id.txtPreuEntrada)

        txtNomEntrada.text = nomTipus ?: "Entrada"
        txtPreuEntrada.text = "$preuTipus €"

        btnAfegir.setOnClickListener {

            if (idTipus == null || nomTipus == null) {
                Toast.makeText(
                    this,
                    "No se ha seleccionado ninguna entrada",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val entrada = TipusEntrada(
                idTipus = idTipus,
                nomTipus = nomTipus,
                preuTipus = preuTipus,
                tipusZona = tipusZona
            )

            CestaManager.afegirEntrada(entrada)

            val totalActualitzat = CestaManager.obtenirTotalElements()
            txtComptador.text = totalActualitzat.toString()

            Toast.makeText(
                this,
                "Entrada afegida a la cistella!",
                Toast.LENGTH_SHORT
            ).show()
        }

        txtComptador.setOnClickListener {
            val intent = Intent(this, CestaActivity::class.java)
            startActivity(intent)
        }
    }
}
