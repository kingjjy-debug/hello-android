package com.kingjjy.hello

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            text = "Hello minimal activity ✅"
            textSize = 20f
            setPadding(48, 48, 48, 48)
        }
        setContentView(tv)

        Toast.makeText(this, "Hello started", Toast.LENGTH_SHORT).show()
    }
}
