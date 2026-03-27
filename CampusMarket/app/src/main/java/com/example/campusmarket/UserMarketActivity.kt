package com.example.campusmarket

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserMarketActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_market)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerUserMarketPosts)

        val dummyList = listOf(
            "상품 1",
            "상품 2",
            "상품 3",
            "상품 4",
            "상품 4",

            "상품 4",

            "상품 4",
            "상품 4",
            "상품 4",
            "상품 4",


            )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MarketAdapter(dummyList)
    }
}