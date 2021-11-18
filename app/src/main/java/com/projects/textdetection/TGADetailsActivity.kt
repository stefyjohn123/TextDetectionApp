package com.projects.textdetection

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.lang.StringBuilder

class TGADetailsActivity : AppCompatActivity() {
    private lateinit var result: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tgadetails)
        result = findViewById(R.id.result);
        getWebsite()
    }

    private fun getWebsite() {
        Thread {
            val builder = StringBuilder()
            try {
                val doc: Document = Jsoup.connect("https://tga-search.clients.funnelback.com/s" +
                        "/search.html?query=AUST+R+46250&collection=tga-artg").get()
                val divElements = doc.getElementsByClass("searchresults clearfloat")
                for (div in divElements) {
                    println(div.text())
                    val ul=div.select("ul").first()
                    val li = ul.select("li")
                    for (i in li.indices) {
                        builder.append(li[i].text()).append("\n")
                    }
                }
            } catch (e: IOException) {
                builder.append("Error : ").append(e.message).append("\n")
            }
            runOnUiThread { result.setText(builder.toString()) }
        }.start()
    }
}