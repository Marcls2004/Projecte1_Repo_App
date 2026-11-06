package com.example.projecte1_repo_app

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

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
            val preuLinia = entrada.preuTipus * quantitat
            preuTotalGlobal += preuLinia


            // Format que es veurà a la llista: Nom + Quantitat + Preu total de la línia
            llistaTextos.add("${entrada.nomTipus} x$quantitat  ->  ${preuLinia} €")
        }

        // 3. Pintem la llista a la pantalla
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, llistaTextos)
        listView.adapter = adapter

        // 4. Mostrem el preu total global
        txtTotalGlobal.text = "Total a pagar: $preuTotalGlobal €"

        // 5. Botó de validar: petició de compra al servidor (Tasca 15)
        btnValidar.setOnClickListener {
            val elements = CestaManager.obtenirElements()
            if (elements.isEmpty()) {
                Toast.makeText(this, "La cistella està buida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: ajusta el nom del fitxer i les claus a com ho va guardar Marc (Tasca 11)
            val prefs = getSharedPreferences("CONFIG", MODE_PRIVATE)
            val nom = prefs.getString("nom", "") ?: ""
            val correu = prefs.getString("correu", "") ?: ""
            val url = prefs.getString("url", "wss://platea3.ieti.site") ?: "wss://platea3.ieti.site"

            val entrades = JSONArray()
            for ((entrada, quantitat) in elements) {
                entrades.put(JSONObject().apply {
                    put("idEsdeveniment", entrada.idEsdeveniment)
                    put("idTipus", entrada.idTipus)
                    put("quantitat", quantitat)
                })
            }

            val missatge = JSONObject().apply {
                put("type", "comprar")
                put("nom", nom)
                put("correu", correu)
                put("entrades", entrades)
            }

            btnValidar.isEnabled = false
            Toast.makeText(this, "Connectant amb el servidor per validar...", Toast.LENGTH_SHORT).show()
            enviarCompra(url, missatge.toString(), btnValidar, listView, txtTotalGlobal)
        }
    }

    private fun enviarCompra(
        url: String, json: String, btn: Button,
        listView: ListView, txtTotal: TextView
    ) {
        try {
            val client = object : WebSocketClient(URI(url)) {

                override fun onOpen(handshakedata: ServerHandshake?) {
                    send(json)
                }

                override fun onMessage(message: String?) {
                    val resp = JSONObject(message ?: return)
                    when (resp.optString("type")) {
                        "welcome" -> { /* el servidor sempre l'envia primer: l'ignorem */ }

                        "compraOk" -> {
                            val locs = resp.getJSONArray("localitzadors")
                            val llistat = (0 until locs.length()).joinToString("\n") { locs.getString(it) }
                            runOnUiThread {
                                CestaManager.buidar()
                                listView.adapter = ArrayAdapter(
                                    this@CestaActivity,
                                    android.R.layout.simple_list_item_1,
                                    emptyList<String>()
                                )
                                txtTotal.text = "Total a pagar: 0.0 €"
                                btn.isEnabled = true
                                mostrarMissatge("Compra realitzada ✅", "Els teus localitzadors:\n\n$llistat")
                            }
                            close()
                        }

                        "error" -> {
                            val msg = resp.optString("message", "Error desconegut")
                            runOnUiThread {
                                btn.isEnabled = true
                                mostrarMissatge("Error ❌", msg)
                            }
                            close()
                        }
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) { }

                override fun onError(ex: Exception?) {
                    runOnUiThread {
                        btn.isEnabled = true
                        mostrarMissatge("Error de connexió ❌", ex?.message ?: "No s'ha pogut connectar")
                    }
                }
            }
            client.connect()
        } catch (e: Exception) {
            btn.isEnabled = true
            mostrarMissatge("Error ❌", e.message ?: "URL no vàlida")
        }
    }

    private fun mostrarMissatge(titol: String, text: String) {
        AlertDialog.Builder(this)
            .setTitle(titol)
            .setMessage(text)
            .setPositiveButton("D'acord", null)
            .show()
    }
}