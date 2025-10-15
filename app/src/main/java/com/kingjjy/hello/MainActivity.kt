package com.kingjjy.hello

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kingjjy.hello.mileage.MileageActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_toast)?.setOnClickListener {
            Toast.makeText(this, "It works 🎉", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnMileage)?.setOnClickListener {
            startActivity(Intent(this, MileageActivity::class.java))
        }
    }
}
