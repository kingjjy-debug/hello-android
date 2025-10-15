package com.example.helloandroid

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // ★ 예전에 잘 가던 주소로 바꾸고 싶으면 아래 2개만 교체하세요.
    private val earnUrl = "https://www.h-point.co.kr/app/point/earn"       // TODO: 이전에 쓰던 정확한 URL로 교체
    private val convertUrl = "https://www.h-point.co.kr/app/mileage/convert" // TODO: 이전에 쓰던 정확한 URL로 교체

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            val btnEarn = findViewById<Button?>(R.id.btnEarnPoint)
            val btnConvert = findViewById<Button?>(R.id.btnConvertToMileage)

            if (btnEarn == null || btnConvert == null) {
                // 레이아웃/ID가 맞지 않아도 앱이 죽지 않게 방어
                Toast.makeText(this, "버튼 레이아웃을 찾지 못했습니다. (안전 모드)", Toast.LENGTH_LONG).show()
                return
            }

            btnEarn.setOnClickListener {
                openExternal(earnUrl)
            }
            btnConvert.setOnClickListener {
                openExternal(convertUrl)
            }

        } catch (t: Throwable) {
            // 어떤 초기화 오류도 여기서 흡수 -> 크래시 방지
            Toast.makeText(this, "초기화 오류: ${t.javaClass.simpleName}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openExternal(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        // 굳이 Chrome 한정(setPackage) 하지 않음: 기본 브라우저로 열리도록
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // 브라우저가 없을 경우 대안(거의 발생하지 않음)
            val chooser = Intent.createChooser(intent, "브라우저 선택")
            startActivity(chooser)
        }
    }
}
