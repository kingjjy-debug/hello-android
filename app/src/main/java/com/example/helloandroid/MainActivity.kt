package com.example.helloandroid

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    companion object {
        // 👉 실제 원하는 목적지 URL로 바꿔도 됩니다.
        private const val URL_EARN_POINT   = "https://m.h-point.co.kr/app/earn"       // 포인트 모으기
        private const val URL_CONVERT_MILE = "https://m.h-point.co.kr/app/convert"    // 마일리지 전환
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 레이아웃 파일명이 다르면 여기만 수정
        setContentView(R.layout.activity_main)

        // 레이아웃에 존재할 법한 여러 후보 ID를 순회해 첫 번째로 발견되는 버튼을 사용
        val btnEarn = findButton(
            "btnEarnPoint", "btn_earn_point", "btnEarn", "buttonEarnPoint", "earnButton"
        )
        val btnConvert = findButton(
            "btnConvertToMileage", "btn_convert_to_mileage", "btnConvert", "buttonConvertToMileage", "convertButton"
        )

        if (btnEarn == null) {
            Toast.makeText(this, "포인트 모으기 버튼 ID를 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
        } else {
            btnEarn.setOnClickListener {
                openInAppWeb("포인트 모으기", URL_EARN_POINT)
            }
        }

        if (btnConvert == null) {
            Toast.makeText(this, "마일리지 전환 버튼 ID를 찾지 못했습니다.", Toast.LENGTH_SHORT).show()
        } else {
            btnConvert.setOnClickListener {
                openInAppWeb("마일리지 전환", URL_CONVERT_MILE)
            }
        }
    }

    private fun findButton(vararg idNames: String): Button? {
        for (name in idNames) {
            val resId = resources.getIdentifier(name, "id", packageName)
            if (resId != 0) {
                val btn = findViewById<Button?>(resId)
                if (btn != null) return btn
            }
        }
        return null
    }

    private fun openInAppWeb(title: String, url: String) {
        val intent = Intent(this, InAppWebActivity::class.java).apply {
            putExtra("title", title)
            putExtra("url", url)
        }
        startActivity(intent)
    }

    // 필요 시 외부 브라우저로 열기
    @Suppress("unused")
    private fun openExternal(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
