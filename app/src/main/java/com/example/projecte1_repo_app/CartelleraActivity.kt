package com.example.projecte1_repo_app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class CartelleraActivity {

    fun llegirUrlServidor(context: Context): String? {
        val fitxer = File(context.filesDir, "CONFIG.XML")
        if (!fitxer.exists()) return null

        return try {
            // Inicializar el constructor de documentos DOM (MP06)
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(fitxer) // Parsear el fichero XML local

            // Buscar la etiqueta <serverUrl>
            val nodeList = document.getElementsByTagName("serverUrl")
            if (nodeList.length > 0) {
                nodeList.item(0).textContent // Devolver la dirección IP / Dominio
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}