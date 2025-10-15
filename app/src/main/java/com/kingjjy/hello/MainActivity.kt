package com.kingjjy.hello

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("HelloApp", "MainActivity onCreate")

        // 회색 배경 + 중앙 텍스트(큰 글씨, 검은색)
        val root = FrameLayout(this).apply {
            setBackgroundColor(0xFFEEEEEE.toInt())
        }
        val tv = TextView(this).apply {
            text = "Hello AppCompat ✅ (step1)"
            textSize = 24f
            setTextColor(0xFF000000.toInt())
            gravity = Gravity.CENTER
        }
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
        root.addView(tv, lp)
        setContentView(root)

        Toast.makeText(this, "MainActivity started", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        Log.i("HelloApp", "MainActivity onResume")
    }
}
