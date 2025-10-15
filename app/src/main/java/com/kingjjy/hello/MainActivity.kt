package com.kingjjy.hello

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this).apply {
            text = "Hello AppCompat ✅"
            textSize = 20f
            setPadding(48, 48, 48, 48)
        }
        setContentView(tv)
    }
}
