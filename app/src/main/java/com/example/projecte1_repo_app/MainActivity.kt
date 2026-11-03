package com.example.projecte1_repo_app

import android.content.Context
import android.content.Intent
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
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val serverField = findViewById<EditText>(R.id.serverField)
        val nameField = findViewById<EditText>(R.id.nameField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val connectButton = findViewById<Button>(R.id.connectButton)
        val statusLabel = findViewById<TextView>(R.id.statusLabel)

        // REQUISITO 11: Comprobar si existe el archivo CONFIG.XML
        val fitxerConfig = File(filesDir, "CONFIG.XML")

        if (fitxerConfig.exists()) {
            statusLabel.text = "Configuració existent trobada. Camps emplenats automàticament."
            statusLabel.setTextColor(Color.parseColor("#27ae60"))
            Toast.makeText(this, "Dades recuperades", Toast.LENGTH_SHORT).show()

            // LEER EL XML Y RELLENAR LOS CONTENEDORES
            try {
                val fis = openFileInput("CONFIG.XML")
                val mapaConfig = parseConfigXml(fis)
                fis.close()

                // Asignamos los valores recuperados a cada EditText
                serverField.setText(mapaConfig["serverUrl"] ?: "")
                nameField.setText(mapaConfig["buyerName"] ?: "")
                emailField.setText(mapaConfig["buyerEmail"] ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
                statusLabel.text = "ERROR: No s'ha pogut llegir el fitxer CONFIG.XML existent."
                statusLabel.setTextColor(Color.RED)
            }
        }

        connectButton.setOnClickListener {
            val url = serverField.text.toString().trim()
            val nom = nameField.text.toString().trim()
            val correu = emailField.text.toString().trim()

            if (url.isEmpty() || nom.isEmpty() || correu.isEmpty()) {
                statusLabel.text = "ERROR: Tots els camps són obligatoris"
                statusLabel.setTextColor(Color.RED)
                Toast.makeText(this, "Falten dades per omplir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            statusLabel.text = "Intentant connectar al servidor..."
            statusLabel.setTextColor(Color.BLUE)

            // Configurar cliente OkHttp para WebSocket
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    // La conexión se realiza en un hilo secundario, pasamos al hilo principal para actualizar la UI
                    runOnUiThread {
                        try {
                            saveConfigXml(this@MainActivity, url, nom, correu)

                            statusLabel.text = "ÈXIT: Connectat correctament a $url\nDades guardades a CONFIG.XML."
                            statusLabel.setTextColor(Color.parseColor("#27ae60"))
                            Toast.makeText(this@MainActivity, "Configuració i connexió amb èxit!", Toast.LENGTH_LONG).show()

                            // Descomenta para ir a la siguiente pantalla
                            /*val intent = Intent(this@MainActivity, CartelleraActivity::class.java)
                            startActivity(intent)
                            finish()*/
                        } catch (e: Exception) {
                            statusLabel.text = "ERROR: Connexió establerta però no s'ha pogut guardar el XML."
                            statusLabel.setTextColor(Color.RED)
                        }
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    // Si el servidor local está apagado o la IP está mal, fallará aquí
                    runOnUiThread {
                        statusLabel.text = "ERROR: No s'ha pogut connectar al servidor.\nMotiu: ${t.message}"
                        statusLabel.setTextColor(Color.RED)
                        Toast.makeText(this@MainActivity, "Error de connexió de xarxa", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    // Función auxiliar para parsear el XML y extraer las etiquetas en un Mapa de strings
    private fun parseConfigXml(inputStream: FileInputStream): Map<String, String> {
        val mapa = mutableMapOf<String, String>()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var eventType = parser.eventType
        var tagActual = ""

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    tagActual = parser.name
                }
                XmlPullParser.TEXT -> {
                    val texto = parser.text.trim()
                    if (texto.isNotEmpty()) {
                        when (tagActual) {
                            "serverUrl" -> mapa["serverUrl"] = texto
                            "buyerName" -> mapa["buyerName"] = texto
                            "buyerEmail" -> mapa["buyerEmail"] = texto
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return mapa
    }

    private fun saveConfigXml(context: Context, url: String, name: String, email: String) {
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = context.openFileOutput("CONFIG.XML", Context.MODE_PRIVATE)

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

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
