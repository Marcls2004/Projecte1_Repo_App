package com.example.projecte1_repo_app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject

// Spec 22: pantalla on l'usuari tria les butaques d'una zona numerada per a un esdeveniment
class SeientsActivity : AppCompatActivity() {

    // Colors de cada estat de butaca: lliure (verd), comprada (gris) i seleccionada (blau)
    private val COLOR_LLIURE = Color.parseColor("#43A047")
    private val COLOR_COMPRAT = Color.parseColor("#9E9E9E")
    private val COLOR_SELECCIONAT = Color.parseColor("#1E88E5")

    // Contenidor on es dibuixa el rectangle de butaques
    private lateinit var layoutSeients: LinearLayout
    // Botó rodó de la cistella amb el número d'elements
    private lateinit var txtComptador: TextView

    // Dades del tipus d'entrada triat a la cartellera
    private var idEsdeveniment = ""
    private var idTipus = ""
    private var nomTipus = ""
    private var preu = 0.0

    // Connexió amb el servidor
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    // Recull les dades de la cartellera, mostra el nom i el preu, i prepara el botó de la cistella
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seients)

        layoutSeients = findViewById(R.id.layoutSeients)
        txtComptador = findViewById(R.id.txtComptadorCesta)

        idEsdeveniment = intent.getStringExtra("idEsdeveniment") ?: ""
        idTipus = intent.getStringExtra("idTipus") ?: ""
        nomTipus = intent.getStringExtra("nomTipus") ?: "Entrada"
        preu = intent.getDoubleExtra("preuTipus", 0.0)

        findViewById<TextView>(R.id.txtNomZona).text = nomTipus
        findViewById<TextView>(R.id.txtPreuSeient).text = "$preu € per butaca"

        txtComptador.setOnClickListener {
            val intentCesta = Intent()
            intentCesta.setClassName(this, "com.example.projecte1_repo_app.CestaActivity")
            startActivity(intentCesta)
        }
    }

    // Demana al servidor (getSeients) l'estat de les butaques de la zona i, quan arriba, les dibuixa
    private fun demanarSeients() {
        val request = Request.Builder().url(ConfigManager.getUrl(this)).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                val peticio = JSONObject()
                peticio.put("type", "getSeients")
                peticio.put("idEsdeveniment", idEsdeveniment)
                peticio.put("idTipus", idTipus)
                webSocket.send(peticio.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val resposta = JSONObject(text)
                when (resposta.optString("type")) {
                    "seients" -> {
                        runOnUiThread { dibuixarSeients(resposta) }
                        webSocket.close(1000, "Seients rebuts")
                    }
                    "error" -> {
                        runOnUiThread {
                            Toast.makeText(this@SeientsActivity,
                                resposta.optString("message"), Toast.LENGTH_LONG).show()
                        }
                        webSocket.close(1000, "Error")
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread {
                    Toast.makeText(this@SeientsActivity,
                        "Error de connexió: ${t.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    // Dibuixa la zona com un rectangle de files x butaques, cada butaca amb el color del seu estat
    private fun dibuixarSeients(dades: JSONObject) {
        val numFiles = dades.getInt("numFiles")
        val seientsPerFila = dades.getInt("seientsPerFila")
        val venuts = dades.getJSONArray("venuts")

        layoutSeients.removeAllViews()
        val mida = (48 * resources.displayMetrics.density).toInt()

        for (f in 1..numFiles) {
            val filaLayout = LinearLayout(this)
            filaLayout.orientation = LinearLayout.HORIZONTAL

            val txtFila = TextView(this)
            txtFila.text = "F$f"
            txtFila.textSize = 16f
            txtFila.width = mida
            filaLayout.addView(txtFila)

            for (s in 1..seientsPerFila) {
                val boto = TextView(this)
                boto.text = "$s"
                boto.textSize = 18f
                boto.setTextColor(Color.WHITE)
                boto.gravity = Gravity.CENTER

                val params = LinearLayout.LayoutParams(mida, mida)
                params.setMargins(6, 6, 6, 6)
                boto.layoutParams = params

                if (estaVenut(venuts, f, s)) {
                    boto.setBackgroundColor(COLOR_COMPRAT)
                } else {
                    pintar(boto, f, s)
                    boto.setOnClickListener {
                        canviarSeleccio(boto, f, s)
                    }
                }
                filaLayout.addView(boto)
            }
            layoutSeients.addView(filaLayout)
        }
    }

    // Retorna true si la butaca és a la llista de venudes que ha enviat el servidor
    private fun estaVenut(venuts: JSONArray, fila: Int, seient: Int): Boolean {
        for (i in 0 until venuts.length()) {
            val v = venuts.getJSONObject(i)
            if (v.getInt("fila") == fila && v.getInt("seient") == seient) {
                return true
            }
        }
        return false
    }

    // En tocar una butaca: si no és a la cistella la posa, i si ja hi és la treu
    private fun canviarSeleccio(boto: TextView, fila: Int, seient: Int) {
        if (CestaManager.esSeleccionat(idEsdeveniment, idTipus, fila, seient)) {
            CestaManager.treureSeient(idEsdeveniment, idTipus, fila, seient)
        } else {
            CestaManager.afegirSeient(
                SeientCistella(idEsdeveniment, idTipus, nomTipus, preu, fila, seient)
            )
        }
        pintar(boto, fila, seient)
        txtComptador.text = CestaManager.obtenirTotalElements().toString()
    }

    // Pinta una butaca de blau si és a la cistella, o de verd si està lliure
    private fun pintar(boto: TextView, fila: Int, seient: Int) {
        if (CestaManager.esSeleccionat(idEsdeveniment, idTipus, fila, seient)) {
            boto.setBackgroundColor(COLOR_SELECCIONAT)
        } else {
            boto.setBackgroundColor(COLOR_LLIURE)
        }
    }

    // Cada cop que es mostra la pantalla actualitza el número de la cistella i torna a demanar les butaques
    override fun onResume() {
        super.onResume()
        txtComptador.text = CestaManager.obtenirTotalElements().toString()
        demanarSeients()
    }

    // En tancar la pantalla tanca la connexió amb el servidor
    override fun onDestroy() {
        webSocket?.close(1000, "Activity tancada")
        super.onDestroy()
    }
}