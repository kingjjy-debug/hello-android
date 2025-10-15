package com.example.helloandroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    companion object {
        // ✅ 버튼별로 "서로 다른" 목적지 URL을 명확히 분리
        // 아래 두 줄을 실제로 원하는 페이지로 교체 가능
        private const val URL_EARN_POINT   = "https://m.h-point.co.kr/app/earn"       // 포인트 모으기용
        private const val URL_CONVERT_MILE = "https://m.h-point.co.kr/app/convert"    // 마일리지 전환용
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // XML에 존재하는 버튼 id 기준 (필요시 id 이름 맞춰 주세요)
        val btnEarn   = findViewById<Button>(R.id.btnEarnPoint)
        val btnConvert= findViewById<Button>(R.id.btnConvertToMileage)

        btnEarn.setOnClickListener {
            openInAppWeb("포인트 모으기", URL_EARN_POINT)
        }

        btnConvert.setOnClickListener {
            openInAppWeb("마일리지 전환", URL_CONVERT_MILE)
        }
    }

    private fun openInAppWeb(title: String, url: String) {
        // InAppWebActivity(WebView)로 열기
        val intent = Intent(this, InAppWebActivity::class.java).apply {
            putExtra("title", title)
            putExtra("url", url)
        }
        startActivity(intent)
    }

    // 만약 외부 브라우저/크롬 커스텀탭으로 바로 열고 싶다면 이 헬퍼를 쓰세요
    @Suppress("unused")
    private fun openExternal(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
