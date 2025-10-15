package com.kingjjy.hello

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // 예전에 잘 가던 외부 브라우저용 URL로 필요한 경우 교체
    private val earnUrl = "https://www.h-point.co.kr/app/point/earn"
    private val convertUrl = "https://www.h-point.co.kr/app/mileage/convert"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEarn = findViewById<Button?>(R.id.btnEarnPoint)
        val btnConvert = findViewById<Button?>(R.id.btnConvertToMileage)

        if (btnEarn == null || btnConvert == null) {
            Toast.makeText(this, "버튼 레이아웃을 찾지 못했습니다. (안전 모드)", Toast.LENGTH_LONG).show()
            return
        }

        btnEarn.setOnClickListener { openExternal(earnUrl) }
        btnConvert.setOnClickListener { openExternal(convertUrl) }
    }

    private fun openExternal(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent) // 기본 브라우저로
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent.createChooser(intent, "브라우저 선택"))
        }
    }
}
