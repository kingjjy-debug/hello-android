package com.kingjjy.hello.mileage

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kingjjy.hello.R
import kotlinx.android.synthetic.main.activity_mileage.*

class MileageActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private val key = "done_ids"

    private val sample = listOf(
        MileageItem("ke-m-checkin", "대한항공 출석체크", "https://www.koreanair.com/"),
        MileageItem("asiana-event", "아시아나 이벤트", "https://flyasiana.com/"),
        MileageItem("card-portal", "카드 포털 미션", "https://card-search.example.com/")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mileage)

        prefs = getSharedPreferences("mileage_prefs", MODE_PRIVATE)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = MileageAdapter(
            context = this,
            items = sample,
            onToggle = { item, checked ->
                val s = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                if (checked) s.add(item.id) else s.remove(item.id)
                prefs.edit().putStringSet(key, s).apply()
            },
            isDone = { item ->
                prefs.getStringSet(key, emptySet())?.contains(item.id) == true
            }
        )
    }
}
