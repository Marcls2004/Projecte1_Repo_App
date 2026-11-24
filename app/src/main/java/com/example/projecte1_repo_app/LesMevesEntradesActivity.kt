package com.example.projecte1_repo_app

import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class LesMevesEntradesActivity : AppCompatActivity() {

    private var webSocket: WebSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_meves_entrades)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val listView: ListView = findViewById(R.id.listViewEntrades)
        val txtEstat: TextView = findViewById(R.id.txtEstatEntrades)

        val url = ConfigManager.getUrl(this)
        val correu = ConfigManager.getCorreu(this)

        val request = Request.Builder().url(url).build()

        webSocket = OkHttpClient().newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                val peticio = JSONObject()
                peticio.put("type", "getEntrades")
                peticio.put("correu", correu)
                webSocket.send(peticio.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val resposta = JSONObject(text)
                if (resposta.optString("type") != "entrades") return   // ignorem el welcome

                val dades = resposta.getJSONArray("data")
                val linies = mutableListOf<String>()

                for (i in 0 until dades.length()) {
                    val e = dades.getJSONObject(i)
                    var linia = e.getString("esdeveniment") + "\n" +
                            "Tipus: " + e.getString("tipus") + "\n"
                    if (e.getString("seient").isNotEmpty()) {
                        linia += "Seient: " + e.getString("seient") + "\n"
                    }
                    linia += "Titular: " + e.getString("titular") + "\n" +
                            "Localitzador: " + e.getString("localitzador")
                    linies.add(linia)
                }

                runOnUiThread {
                    txtEstat.text = if (linies.isEmpty()) "Encara no tens entrades" else ""
                    listView.adapter = ArrayAdapter(
                        this@LesMevesEntradesActivity,
                        android.R.layout.simple_list_item_1,
                        linies
                    )
                }
                webSocket.close(1000, "Entrades rebudes")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                runOnUiThread {
                    txtEstat.text = "Error de connexió: ${t.message}"
                }
            }
        })
    }
}