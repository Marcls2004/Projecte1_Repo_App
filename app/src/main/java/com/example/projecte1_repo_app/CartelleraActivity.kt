package com.example.projecte1_repo_app

import android.os.Bundle
import android.util.Xml
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.xmlpull.v1.XmlPullParser
import java.io.FileInputStream

class CartelleraActivity : AppCompatActivity() {

    private lateinit var recyclerCartellera: RecyclerView
    private lateinit var navUser: TextView

    private lateinit var adapterCartellera: CartelleraAdapter
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cartellera)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.mainCartellera)
        ) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        navUser = findViewById(R.id.navUser)
        recyclerCartellera = findViewById(R.id.recyclerCartellera)

        adapterCartellera = CartelleraAdapter(emptyList())
        recyclerCartellera.layoutManager = LinearLayoutManager(this)
        recyclerCartellera.adapter = adapterCartellera

        recuperarNombreUsuario()

        val serverUrl = intent.getStringExtra("SERVER_URL")
            ?: "ws://10.0.2.2:8888"

        conectarWebSocket(serverUrl)
    }

    private fun conectarWebSocket(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(
            request,
            object : WebSocketListener() {

                override fun onOpen(
                    webSocket: WebSocket,
                    response: Response
                ) {
                    webSocket.send("""{"type":"getEsdeveniments"}""")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {

                    android.util.Log.d("WEBSOCKET", "MENSAJE RECIBIDO: $text")

                    runOnUiThread {
                        try {
                            val gson = Gson()

                            val respuesta = gson.fromJson(
                                text,
                                com.google.gson.JsonObject::class.java
                            )

                            android.util.Log.d(
                                "WEBSOCKET",
                                "TIPO RECIBIDO: ${respuesta.get("type")}"
                            )

                            when (respuesta.get("type")?.asString) {

                                "esdeveniments" -> {

                                    val itemType =
                                        object : TypeToken<List<Esdeveniment>>() {}.type

                                    val lista: List<Esdeveniment> = gson.fromJson(
                                        respuesta.getAsJsonArray("data"),
                                        itemType
                                    )

                                    android.util.Log.d(
                                        "WEBSOCKET",
                                        "EVENTOS CONVERTIDOS: ${lista.size}"
                                    )

                                    adapterCartellera.updateData(lista)
                                }

                                "error" -> {
                                    android.util.Log.e(
                                        "WEBSOCKET",
                                        "ERROR SERVIDOR: ${respuesta.get("message")}"
                                    )
                                }

                                else -> {
                                    android.util.Log.d(
                                        "WEBSOCKET",
                                        "OTRO MENSAJE: $text"
                                    )
                                }
                            }

                        } catch (e: Exception) {
                            android.util.Log.e(
                                "WEBSOCKET",
                                "ERROR PROCESANDO JSON",
                                e
                            )
                        }
                    }
                }

                override fun onFailure(
                    webSocket: WebSocket,
                    t: Throwable,
                    response: Response?
                ) {
                    runOnUiThread {
                        Toast.makeText(
                            this@CartelleraActivity,
                            "Error de conexión: ${t.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        )
    }

    private fun recuperarNombreUsuario() {
        try {
            val fis: FileInputStream = openFileInput("CONFIG.XML")
            val parser = Xml.newPullParser()
            parser.setInput(fis, "UTF-8")

            var eventType = parser.eventType
            var tagActual = ""
            var username = "User"

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        tagActual = parser.name
                    }

                    XmlPullParser.TEXT -> {
                        val texto = parser.text.trim()

                        if (texto.isNotEmpty() && tagActual == "buyerName") {
                            username = texto
                        }
                    }
                }

                eventType = parser.next()
            }

            fis.close()
            navUser.text = username

        } catch (e: Exception) {
            e.printStackTrace()
            navUser.text = "User"
        }
    }

    override fun onDestroy() {
        webSocket?.close(1000, "Activity tancada")
        super.onDestroy()
    }
}