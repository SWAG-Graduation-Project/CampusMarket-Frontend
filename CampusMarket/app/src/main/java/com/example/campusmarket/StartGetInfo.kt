package com.example.campusmarket

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StartGetInfo : AppCompatActivity() {

    private lateinit var webViewMap: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_start_get_info)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnSkip = findViewById<Button>(R.id.tvSkip)

        btnSkip.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            startActivity(intent)
        }



        val btnNext = findViewById<Button>(R.id.btnNext)
        webViewMap = findViewById(R.id.webViewMap)

        webViewMap.settings.javaScriptEnabled = true
        webViewMap.settings.domStorageEnabled = true
        webViewMap.settings.useWideViewPort = true
        webViewMap.settings.loadWithOverviewMode = true
        webViewMap.settings.setSupportZoom(true)
        webViewMap.settings.builtInZoomControls = true
        webViewMap.settings.displayZoomControls = false

        webViewMap.webViewClient = WebViewClient()
        webViewMap.webChromeClient = WebChromeClient()

        webViewMap.loadUrl("https://map-web-sigma.vercel.app/")

        btnNext.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onBackPressed() {
        if (::webViewMap.isInitialized && webViewMap.canGoBack()) {
            webViewMap.goBack()
        } else {
            super.onBackPressed()
        }
    }




}