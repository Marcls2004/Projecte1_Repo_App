package com.example.projecte1_repo_app

import android.app.AlertDialog
import android.os.Bundle
import android.util.Xml
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import java.net.URI

class CestaActivity : AppCompatActivity() {

    private lateinit var adapter: CestaAdapter
    private lateinit var txtTotalGlobal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cesta)

        val listView: ListView = findViewById(R.id.listViewCesta)
        txtTotalGlobal = findViewById(R.id.txtTotalGlobal)
        val btnValidar: Button = findViewById(R.id.btnValidarCesta)

        adapter = CestaAdapter(
            linies = construirLinies(),
            onMes = { entrada ->
                CestaManager.canviarQuantitat(entrada, +1)
                refrescar()
            },
            onMenys = { linia ->
                if (linia.quantitat > 1) {
                    CestaManager.canviarQuantitat(linia.entrada, -1)
                    refrescar()
                } else {
                    confirmar("Vols treure \"${linia.entrada.nomTipus}\" de la cistella?") {
                        CestaManager.eliminarEntrada(linia.entrada)
                        refrescar()
                    }
                }
            },
            onEliminarButaca = { butaca ->
                confirmar("Vols treure la fila ${butaca.fila}, butaca ${butaca.numero} de la cistella?") {
                    CestaManager.eliminarButaca(butaca)
                    refrescar()
                }
            }
        )
        listView.adapter = adapter
        refrescar()

        btnValidar.setOnClickListener {
            val elements = CestaManager.obtenirElements()
            val butaques = CestaManager.obtenirButaques()

            if (elements.isEmpty() && butaques.isEmpty()) {
                Toast.makeText(this, "La cistella està buida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val config = llegirConfig()
            val nom = config["buyerName"] ?: ""
            val correu = config["buyerEmail"] ?: ""
            val url = config["serverUrl"] ?: "wss://platea3.ieti.site"

            val entrades = JSONArray()
            for ((entrada, quantitat) in elements) {
                entrades.put(JSONObject().apply {
                    put("idEsdeveniment", entrada.idEsdeveniment)
                    put("idTipus", entrada.idTipus)
                    put("quantitat", quantitat)
                })
            }

            val seients = JSONArray()
            for (b in butaques) {
                seients.put(JSONObject().apply {
                    put("idEsdeveniment", b.idEsdeveniment)
                    put("idTipus", b.idTipus)
                    put("fila", b.fila)
                    put("seient", b.numero)
                })
            }

            val missatge = JSONObject().apply {
                put("type", "comprar")
                put("nom", nom)
                put("correu", correu)
                put("entrades", entrades)
                put("seients", seients)
            }

            CestaManager.netejarErrorsButaques()
            refrescar()

            btnValidar.isEnabled = false
            Toast.makeText(this, "Connectant amb el servidor per validar...", Toast.LENGTH_SHORT).show()
            enviarCompra(url, missatge.toString(), btnValidar)
        }
    }

    private fun construirLinies(): List<LiniaCesta> =
        CestaManager.obtenirElements().map { (entrada, quantitat) -> LiniaCesta.General(entrada, quantitat) } +
                CestaManager.obtenirButaques().map { LiniaCesta.Butaca(it) }

    private fun refrescar() {
        adapter.actualitzar(construirLinies())
        val total = CestaManager.obtenirElements().entries.sumOf { it.key.preuTipus * it.value } +
                CestaManager.obtenirButaques().sumOf { it.preu }
        txtTotalGlobal.text = "Total a pagar: $total €"
    }

    private fun confirmar(missatge: String, onSi: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage(missatge)
            .setPositiveButton("Eliminar") { _, _ -> onSi() }
            .setNegativeButton("Cancel·lar", null)
            .show()
    }

    private fun enviarCompra(url: String, json: String, btn: Button) {
        try {
            val client = object : WebSocketClient(URI(url)) {

                override fun onOpen(handshakedata: ServerHandshake?) {
                    send(json)
                }

                override fun onMessage(message: String?) {
                    val resp = JSONObject(message ?: return)
                    when (resp.optString("type")) {
                        "welcome" -> { }

                        "compraOk" -> {
                            val locs = resp.getJSONArray("localitzadors")
                            val llistat = (0 until locs.length()).joinToString("\n") { locs.getString(it) }
                            runOnUiThread {
                                CestaManager.buidar()
                                refrescar()
                                btn.isEnabled = true
                                mostrarMissatge("Compra realitzada", "Els teus localitzadors:\n\n$llistat")
                            }
                            close()
                        }

                        "error" -> {
                            val msg = resp.optString("message", "Error desconegut")
                            val rebutjats = resp.optJSONArray("seientsRebutjats")

                            val clausError = mutableSetOf<String>()
                            val detall = StringBuilder()

                            if (rebutjats != null) {
                                for (i in 0 until rebutjats.length()) {
                                    val s = rebutjats.getJSONObject(i)
                                    val idEsde = s.optString("idEsdeveniment")
                                    val idTip = s.optString("idTipus")
                                    val fila = s.optInt("fila")
                                    val seient = s.optInt("seient")

                                    clausError.add("$idEsde|$idTip|$fila|$seient")
                                    detall.append("\n- Fila $fila, Butaca $seient ($idTip)")
                                }
                            }

                            runOnUiThread {
                                CestaManager.marcarErrorsButaques(clausError)
                                refrescar()
                                btn.isEnabled = true

                                val textFinal = if (detall.isNotEmpty()) "$msg:\n$detall" else msg
                                mostrarMissatge("Error", textFinal)
                            }
                            close()
                        }
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) { }

                override fun onError(ex: Exception?) {
                    runOnUiThread {
                        btn.isEnabled = true
                        mostrarMissatge("Error de connexió", ex?.message ?: "No s'ha pogut connectar")
                    }
                }
            }
            client.connect()
        } catch (e: Exception) {
            btn.isEnabled = true
            mostrarMissatge("Error", e.message ?: "URL no vàlida")
        }
    }

    private fun llegirConfig(): Map<String, String> {
        val mapa = mutableMapOf<String, String>()
        try {
            openFileInput("CONFIG.XML").use { fis ->
                val parser = Xml.newPullParser()
                parser.setInput(fis, "UTF-8")
                var tag = ""
                var ev = parser.eventType
                while (ev != XmlPullParser.END_DOCUMENT) {
                    when (ev) {
                        XmlPullParser.START_TAG -> tag = parser.name
                        XmlPullParser.TEXT -> if (parser.text.isNotBlank()) mapa[tag] = parser.text.trim()
                    }
                    ev = parser.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mapa
    }

    private fun mostrarMissatge(titol: String, text: String) {
        AlertDialog.Builder(this)
            .setTitle(titol)
            .setMessage(text)
            .setPositiveButton("D'acord", null)
            .show()
    }
}
