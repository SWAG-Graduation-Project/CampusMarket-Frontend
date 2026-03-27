package com.example.campusmarket

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MarketActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_market)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnSort = findViewById<Button>(R.id.btnSort)

        btnSort.setOnClickListener {
            val popup = PopupMenu(this, btnSort)
            popup.menuInflater.inflate(R.menu.menu_sort, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.sort_popular -> btnSort.text = "인기순"
                    R.id.sort_latest -> btnSort.text = "최신순"
                    R.id.sort_price_low -> btnSort.text = "조회순"
                }
                true
            }

            popup.show()
        }

        val btnList = findViewById<Button>(R.id.btnList)
        val btnAll = findViewById<Button>(R.id.btnAll)

        btnAll.setOnClickListener {
            val intent = Intent(this, ProductDetailActivity::class.java)
            startActivity(intent)
        }
    }
    private fun showMarketPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_market_detail)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        val window = dialog.window!!

        // 👉 너비 설정
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // 👉 중앙 정렬 + 위치 보정
        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.y = 0   // 👉 중요 (위치 보정)
        window.attributes = params

        // 👉 배경 어둡게 (이미 잘됨)
        window.setDimAmount(0.6f)
    }


}