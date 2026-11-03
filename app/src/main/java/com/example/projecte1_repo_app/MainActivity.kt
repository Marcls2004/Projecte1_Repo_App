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
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter

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

        // REQUISITO 11: Comprobar si existe el archivo CONFIG.XML (Fíjate en las mayúsculas del nombre)
        val fitxerConfig = File(filesDir, "CONFIG.XML")

        if (fitxerConfig.exists()) {
            statusLabel.text = "Configuració existent trobada a CONFIG.XML.\nConnectant de forma automàtica..."
            statusLabel.setTextColor(Color.parseColor("#27ae60"))
            Toast.makeText(this, "Sessió restaurada automàticament", Toast.LENGTH_SHORT).show()

            // TODO: Aquí saltaremos directamente a la Tarea 12 (Cartellera) en el futuro
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

            try {
                // CORRECCIÓN: Ahora pasamos 'this' como contexto
                saveConfigXml(this, url, nom, correu)

                statusLabel.text = "ÈXIT: Dades guardades correctament a CONFIG.XML.\nConnectat al servidor: $url"
                statusLabel.setTextColor(Color.parseColor("#27ae60"))
                Toast.makeText(this, "Configuració guardada amb èxit!", Toast.LENGTH_LONG).show()

                // TODO: Tras guardar con éxito, también podemos abrir la Cartellera (Tarea 12)
                val intent = Intent(this, CartelleraActivity::class.java)
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                statusLabel.text = "ERROR: No s'ha pogut guardar o connectar.\nMotiu: ${e.message}"
                statusLabel.setTextColor(Color.RED)
                Toast.makeText(this, "Error en el primer inici", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Mantenemos la función recibiendo el Context para cumplir con MP06 correctamente
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
            throw e // Lanzamos la excepción para que el catch del botón la capture e informe del error
        }
    }
}
