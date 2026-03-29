package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val homebutton = findViewById<LinearLayout>(R.id.gohome)

        homebutton.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            startActivity(intent)
        }

        val goMymarket = findViewById<LinearLayout>(R.id.goMymarket)

        goMymarket.setOnClickListener {
            val intent = Intent(this, MyMarketActivity::class.java)
            startActivity(intent)
        }

        val goMypage = findViewById<LinearLayout>(R.id.gomypage)

        goMypage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        val gochat = findViewById<LinearLayout>(R.id.gochat)

        gochat.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }


        val dummyList = listOf(
            ChatPost(
                "상품 이름 상품 이름상품 이름",
                "파닥부단",
                "07.21",
                "안녕하세요~ 혹시 아직 판매 중인가요?",
                R.drawable.chat_brawn
            ),
            ChatPost(
                "상품 이름 상품 이름상품 이름",
                "북북북단",
                "07.21",
                "조금만 깎아 주세요.",
                R.drawable.chat_brawn
            ),
            ChatPost(
                "상품 이름 상품 이름상품 이름",
                "테스트 상품",
                "07.20",
                "거래 가능하신가요?",
                R.drawable.chat_brawn
            )
        )
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerChat)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ChatAdapter(dummyList)
    }
}