package com.kingjjy.hello

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kingjjy.hello.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 화면에 "Hello Android!" 표시 (strings.xml의 값)
        binding.tvHello.text = getString(R.string.hello_text)
    }
}
