package com.example.helloandroid

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class InAppWebActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url") ?: "https://www.h-point.co.kr/"
        setContent {
            MaterialTheme {
                var title by remember { mutableStateOf("로딩 중...") }
                var webViewRef by remember { mutableStateOf<WebView?>(null) }

                // 안드로이드 뒤로가기 = WebView 뒤로가기
                BackHandler(enabled = webViewRef?.canGoBack() == true) {
                    webViewRef?.goBack()
                }

                Scaffold(
                    topBar = { TopAppBar(title = { Text(title) }) }
                ) { padding ->
                    AndroidView(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webChromeClient = object : WebChromeClient() {
                                    override fun onReceivedTitle(view: WebView?, t: String?) {
                                        t?.let { title = it }
                                    }
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        // 인앱에서 계속 열기
                                        return false
                                    }
                                }
                                loadUrl(url)
                                webViewRef = this
                            }
                        }
                    )
                }
            }
        }
    }
}
