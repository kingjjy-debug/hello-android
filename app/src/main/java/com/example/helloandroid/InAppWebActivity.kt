package com.example.helloandroid

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URISyntaxException

class InAppWebActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startUrl = intent.getStringExtra("startUrl")
            ?: "https://www.h-point.co.kr/"

        setContent {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        setupForApp(this)
                        loadUrl(startUrl)
                    }
                },
                update = { webView ->
                    // 같은 액티비티 재사용 시 중복 로드 방지
                    if (webView.url.isNullOrEmpty()) webView.loadUrl(startUrl)
                }
            )
            LaunchedEffect(Unit) { /* no-op */ }
        }
    }
}

/** WebView 기본 설정 + 딥링크/커스텀 스킴 처리 */
private fun setupForApp(webView: WebView) {
    with(webView.settings) {
        javaScriptEnabled = true
        domStorageEnabled = true
        loadsImagesAutomatically = true
        databaseEnabled = true
        setSupportMultipleWindows(true)
        javaScriptCanOpenWindowsAutomatically = true
        useWideViewPort = true
        loadWithOverviewMode = true
        userAgentString = userAgentString + " APP-WebView"
    }

    webView.webChromeClient = object : WebChromeClient() {
        // window.open / target=_blank 처리
        override fun onCreateWindow(
            view: WebView?,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message?
        ): Boolean {
            val ctx = view?.context ?: return false
            val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
            val temp = WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        v: WebView,
                        request: WebResourceRequest
                    ): Boolean = handleDeepLink(ctx, request.url.toString())

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(v: WebView, url: String): Boolean =
                        handleDeepLink(ctx, url)
                }
            }
            transport.webView = temp
            resultMsg.sendToTarget()
            return true
        }
    }

    webView.webViewClient = object : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            // 필요하면 커스텀 에러 화면 처리 가능
            super.onReceivedError(view, request, error)
        }

        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean = handleDeepLink(view.context, request.url.toString())

        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
            handleDeepLink(view.context, url)
    }
}

/** 커스텀 스킴/intent/마켓 링크를 앱으로 넘겨주고, 설치 안되어 있으면 스토어로 폴백 */
private fun handleDeepLink(ctx: Context, url: String): Boolean {
    // 1) 일반 http/https는 WebView에서 계속 열기
    if (url.startsWith("http://") || url.startsWith("https://")) return false

    return try {
        when {
            // 2) intent:// 처리
            url.startsWith("intent://") -> {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    component = null // 보안상 명시적 컴포넌트 제거
                    flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                try {
                    ctx.startActivity(intent)
                    true
                } catch (e: ActivityNotFoundException) {
                    // 설치 안됨 → Play 스토어로 이동 (browser_fallback_url 우선)
                    val fallback = intent.getStringExtra("browser_fallback_url")
                    val pkg = intent.`package`
                    when {
                        !fallback.isNullOrEmpty() -> {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallback)))
                            true
                        }
                        !pkg.isNullOrEmpty() -> {
                            openMarket(ctx, pkg); true
                        }
                        else -> true // 막힌 링크는 소거
                    }
                }
            }

            // 3) 마켓 링크
            url.startsWith("market://") -> {
                try {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (_: Exception) {
                    // https 스토어로 폴백 시도 (패키지 파싱 실패 시에는 그냥 무시)
                    val pkg = Uri.parse(url).getQueryParameter("id")
                    if (!pkg.isNullOrEmpty()) openMarket(ctx, pkg)
                }
                true
            }

            // 4) 그 외 커스텀 스킴(hpoint://, kakaotalk:// 등)
            else -> {
                try {
                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE)
                        flags = flags or Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                    true
                } catch (_: ActivityNotFoundException) {
                    // 패키지를 알 수 없으면 조용히 무시
                    true
                }
            }
        }
    } catch (_: URISyntaxException) {
        true
    }
}

private fun openMarket(ctx: Context, pkg: String) {
    // 1차: 앱스토어 앱
    try {
        ctx.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        return
    } catch (_: Exception) { /* fallthrough */ }

    // 2차: 웹 스토어
    try {
        ctx.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Exception) { /* give up */ }
}
