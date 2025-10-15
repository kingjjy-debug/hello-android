package com.kingjjy.hello

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnEarnPoint).setOnClickListener {
            openExternal("https://www.h-point.co.kr/earn")          // 필요시 URL 바꿔도 됨
        }
        findViewById<Button>(R.id.btnConvertToMileage).setOnClickListener {
            openExternal("https://www.h-point.co.kr/convert")       // 필요시 URL 바꿔도 됨
        }
    }

    private fun openExternal(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
