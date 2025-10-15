package com.example.helloandroid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class InAppWebActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_URL = "extra_url"

        fun start(context: Context, url: String) {
            val i = Intent(context, InAppWebActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
            if (context !is Activity) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(i)
        }
    }

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        val url = intent.getStringExtra(EXTRA_URL) ?: "about:blank"

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val target = request?.url ?: return false
                val scheme = target.scheme ?: ""

                // 앱/마켓/인텐트 스킴은 외부로 넘김 (H.Point 앱 진입 허용)
                if (scheme.equals("intent", true) ||
                    scheme.equals("market", true) ||
                    scheme.equals("hpointapp", true)
                ) {
                    openExternal(target)
                    return true
                }

                // https/http는 인앱에서 계속
                return false
            }
        }

        webView.loadUrl(url)
    }

    private fun openExternal(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // market:// 처리 실패시 플레이스토어 웹으로 열기 시도
            if (uri.scheme == "market") {
                val fallback = Uri.parse("https://play.google.com/store/apps/details?${uri.encodedQuery ?: ""}")
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, fallback))
                } catch (_: Exception) { /* ignore */ }
            }
        }
    }

    override fun onBackPressed() {
        if (this::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
