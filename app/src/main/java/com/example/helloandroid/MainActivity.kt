package com.example.helloandroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // 🔗 여기에 "정확히 원하는 목적지" URL을 넣어주세요 (HTTPS)
    private val EARN_HTTPS_URL = "https://PUT_EARN_URL_HERE"
    private val CONVERT_HTTPS_URL = "https://PUT_CONVERT_URL_HERE"

    // 🔗 H.Point 앱 딥링크 예시 (실제 스킴/패스는 사용하던 값으로 교체)
    // 예: "hpointapp://earn" 또는 "hpointapp://convert" 형태라면 아래 교체
    private val EARN_APP_DEEPLINK = "hpointapp://earn"
    private val CONVERT_APP_DEEPLINK = "hpointapp://convert"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEarn = findViewById<Button>(R.id.btnEarnPoint)
        val btnConvert = findViewById<Button>(R.id.btnConvertToMileage)

        btnEarn.setOnClickListener {
            openHPointPreferApp(
                appLink = EARN_APP_DEEPLINK,
                httpsFallback = EARN_HTTPS_URL
            )
        }

        btnConvert.setOnClickListener {
            openHPointPreferApp(
                appLink = CONVERT_APP_DEEPLINK,
                httpsFallback = CONVERT_HTTPS_URL
            )
        }
    }

    /**
     * 1) 앱 딥링크 먼저 시도
     * 2) 실패하면 앱 내 WebView(InAppWebActivity)로 HTTPS 열기
     */
    private fun openHPointPreferApp(appLink: String, httpsFallback: String) {
        val appUri = Uri.parse(appLink)
        val appIntent = Intent(Intent.ACTION_VIEW, appUri)
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(appIntent)
        } catch (_: ActivityNotFoundException) {
            // 앱이 없거나 딥링크가 막힌 경우: 앱 내 WebView로 HTTPS 열기
            InAppWebActivity.start(this, httpsFallback)
        }
    }
}
