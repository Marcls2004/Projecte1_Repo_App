package com.example.projecte1_repo_app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Xml
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.StringWriter

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_config)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val serverField = findViewById<EditText>(R.id.etUrl)
        val nameField = findViewById<EditText>(R.id.etNombre)
        val connectButton = findViewById<Button>(R.id.btnGuardar)
        val statusLabel = findViewById<TextView>(R.id.statusLabel)

        val fitxerConfig = File(filesDir, "CONFIG.XML")
        var correuActual = "" // Aquí guardamos el correo para no perderlo

        if (fitxerConfig.exists()) {
            statusLabel.text = "Configuració existent trobada."
            statusLabel.setTextColor(Color.parseColor("#27ae60"))

            try {
                val fis = openFileInput("CONFIG.XML")
                val mapaConfig = parseConfigXml(fis)
                fis.close()

                serverField.setText(mapaConfig["serverUrl"] ?: "")
                nameField.setText(mapaConfig["buyerName"] ?: "")
                correuActual = mapaConfig["buyerEmail"] ?: "" // Rescatamos el correo
            } catch (e: Exception) {
                e.printStackTrace()
                statusLabel.text = "ERROR: No s'ha pogut llegir CONFIG.XML"
                statusLabel.setTextColor(Color.RED)
            }
        }

        connectButton.setOnClickListener {
            val novaUrl = serverField.text.toString().trim()
            val nouNom = nameField.text.toString().trim()

            // 1. Comprobar campos vacíos
            if (novaUrl.isEmpty() || nouNom.isEmpty()) {
                statusLabel.text = "ERROR: El nom i la URL són obligatoris."
                statusLabel.setTextColor(Color.RED)
                return@setOnClickListener
            }

            // 2. Comprobar formato básico de WebSocket (ws:// o wss://)
            if ((!novaUrl.startsWith("ws://") && !novaUrl.startsWith("wss://")) || novaUrl.length <= 6) {
                statusLabel.text = "ERROR: La URL ha de començar per ws:// o wss://"
                statusLabel.setTextColor(Color.RED)
                return@setOnClickListener
            }

            // Bloqueamos el botón mientras comprueba la conexión
            connectButton.isEnabled = false
            statusLabel.text = "Comprovant connexió amb el servidor..."
            statusLabel.setTextColor(Color.DKGRAY)

            // 3. Llamamos a la función para probar la conexión real
            comprovarConnexioWs(novaUrl) { connexioCorrecta ->
                runOnUiThread {
                    connectButton.isEnabled = true

                    if (!connexioCorrecta) {
                        statusLabel.text = "ERROR: No s'ha pogut connectar a aquesta URL."
                        statusLabel.setTextColor(Color.RED)
                        return@runOnUiThread // No guarda ni te deja salir de aquí
                    }

                    // 4. Solo si conecta bien, guardamos el XML y cerramos la pantalla
                    try {
                        saveConfigXml(this, novaUrl, nouNom, correuActual)
                        Toast.makeText(this, "Configuració guardada", Toast.LENGTH_SHORT).show()
                        finish()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        statusLabel.text = "ERROR: No s'ha pogut guardar el fitxer."
                        statusLabel.setTextColor(Color.RED)
                    }
                }
            }
        }
    }

    // La función se declara fuera de onCreate, al mismo nivel que parseConfigXml y saveConfigXml
    private fun comprovarConnexioWs(url: String, callback: (Boolean) -> Unit) {
        try {
            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(4, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val request = okhttp3.Request.Builder().url(url).build()

            client.newWebSocket(request, object : okhttp3.WebSocketListener() {
                override fun onOpen(webSocket: okhttp3.WebSocket, response: okhttp3.Response) {
                    webSocket.close(1000, "Prova correcta")
                    callback(true)
                }

                override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: okhttp3.Response?) {
                    callback(false)
                }
            })
        } catch (e: Exception) {
            // Captura errores si la URL tiene caracteres ilegales o mal formato
            callback(false)
        }
    }

    private fun parseConfigXml(inputStream: FileInputStream): Map<String, String> {
        val mapa = mutableMapOf<String, String>()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var tagActual = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> tagActual = parser.name
                XmlPullParser.TEXT -> {
                    val texto = parser.text.trim()
                    if (texto.isNotEmpty()) {
                        mapa[tagActual] = texto
                    }
                }
            }
            eventType = parser.next()
        }
        return mapa
    }

    private fun saveConfigXml(context: Context, url: String, name: String, email: String) {
        val fileOutputStream: FileOutputStream = context.openFileOutput("CONFIG.XML", Context.MODE_PRIVATE)
        val serializer = Xml.newSerializer()
        val writer = StringWriter()

        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        serializer.startTag("", "configuration")

        serializer.startTag("", "serverUrl")
        serializer.text(url)
        serializer.endTag("", "serverUrl")

        serializer.startTag("", "buyerName")
        serializer.text(name)
        serializer.endTag("", "buyerName")

        serializer.startTag("", "buyerEmail")
        serializer.text(email)
        serializer.endTag("", "buyerEmail")

        serializer.endTag("", "configuration")
        serializer.endDocument()

        fileOutputStream.write(writer.toString().toByteArray())
        fileOutputStream.close()
    }
}
