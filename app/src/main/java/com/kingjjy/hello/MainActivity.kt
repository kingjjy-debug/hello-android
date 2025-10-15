package com.kingjjy.hello

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kingjjy.hello.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 실제 열릴 URL
        val earnPointUrl = "https://www.h-point.co.kr/event"      // 포인트 모으기
        val mileageUrl   = "https://www.h-point.co.kr/mileage"    // 마일리지 전환

        binding.btnEarnPoint.setOnClickListener {
            openExternal(earnPointUrl)
        }
        binding.btnConvertToMileage.setOnClickListener {
            openExternal(mileageUrl)
        }
    }

    private fun openExternal(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }
}
