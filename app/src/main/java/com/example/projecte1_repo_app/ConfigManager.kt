package com.example.projecte1_repo_app

import android.content.Context
import android.util.Xml
import org.xmlpull.v1.XmlPullParser

// Llegeix el CONFIG.XML que desa MainActivity (serverUrl, buyerName, buyerEmail)
object ConfigManager {

    private fun llegir(context: Context): Map<String, String> {
        val mapa = mutableMapOf<String, String>()
        try {
            context.openFileInput("CONFIG.XML").use { fis ->
                val parser = Xml.newPullParser()
                parser.setInput(fis, "UTF-8")
                var event = parser.eventType
                var tag = ""
                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> tag = parser.name
                        XmlPullParser.TEXT -> {
                            val text = parser.text.trim()
                            if (text.isNotEmpty()) mapa[tag] = text
                        }
                    }
                    event = parser.next()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mapa
    }

    fun getUrl(context: Context): String = llegir(context)["serverUrl"] ?: "wss://platea3.ieti.site"
    fun getNom(context: Context): String = llegir(context)["buyerName"] ?: ""
    fun getCorreu(context: Context): String = llegir(context)["buyerEmail"] ?: ""
}