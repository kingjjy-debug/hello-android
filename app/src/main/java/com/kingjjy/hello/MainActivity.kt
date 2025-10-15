package com.kingjjy.hello

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 런타임 진입 확인용 마커
        findViewById<TextView>(R.id.helloText).text = "Hello is running ✅"
    }
}
