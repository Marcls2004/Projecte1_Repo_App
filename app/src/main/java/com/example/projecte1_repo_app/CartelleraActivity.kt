package com.example.projecte1_repo_app

import android.os.Bundle
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import org.xmlpull.v1.XmlPullParser
import java.io.FileInputStream

// 1. LA CLASE DE LA ACTIVIDAD PRINCIPAL
class CartelleraActivity : AppCompatActivity() {

    private lateinit var webSocket: WebSocket
    private val client = OkHttpClient()

    private lateinit var recyclerCartellera: RecyclerView
    private lateinit var adapterCartellera: CartelleraAdapter
    private lateinit var navUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cartellera)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainCartellera)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        navUser = findViewById(R.id.navUser)
        recyclerCartellera = findViewById(R.id.recyclerCartellera)

        recyclerCartellera.layoutManager = LinearLayoutManager(this)
        adapterCartellera = CartelleraAdapter(emptyList())
        recyclerCartellera.adapter = adapterCartellera

        recuperarNombreUsuario()

        val serverUrl = intent.getStringExtra("SERVER_URL") ?: "ws://10.0.2.2:8888"
        conectarWebSocket(serverUrl)
    }

    private fun conectarWebSocket(url: String) {
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("GET_CARTELLERA")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                runOnUiThread {
                    try {
                        val gson = Gson()
                        val itemType = object : TypeToken<List<Esdeveniment>>() {}.type
                        val llistaEsdeveniments: List<Esdeveniment> = gson.fromJson(text, itemType)
                        adapterCartellera.updateData(llistaEsdeveniments)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread {
                    Toast.makeText(this@CartelleraActivity, "Error de connexió", Toast.LENGTH_SHORT).show()
                }
            }
        })
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
                    XmlPullParser.START_TAG -> tagActual = parser.name
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
        super.onDestroy()
        if (::webSocket.isInitialized) {
            webSocket.close(1000, "Activity tancada")
        }
    }
}

// 2. EL ADAPTADOR DE LA LISTA CORREGIDO (Alineado con .nom)
class CartelleraAdapter(private var llista: List<Esdeveniment>) :
    RecyclerView.Adapter<CartelleraAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tTitol: TextView = view.findViewById(R.id.textTitol)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_esdeveniment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val esdeveniment = llista[position]

        // CORRECCIÓN AQUÍ: Ahora llama a .nom en lugar de .titol para coincidir con tu modelo
        holder.tTitol.text = esdeveniment.nom
    }

    override fun getItemCount(): Int = llista.size

    fun updateData(novaLlista: List<Esdeveniment>) {
        this.llista = novaLlista
        notifyDataSetChanged()
    }
}
