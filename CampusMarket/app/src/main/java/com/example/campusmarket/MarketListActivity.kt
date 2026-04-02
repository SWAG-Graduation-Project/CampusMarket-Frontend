package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusmarket.data.model.MajorCategory
import kotlinx.coroutines.launch

class MarketListActivity : AppCompatActivity() {

    private lateinit var btnMarket: Button
    private lateinit var categoryContainer: LinearLayout
    private lateinit var recyclerView: RecyclerView

    private var selectedCategoryButton: Button? = null
    private var selectedCategoryId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_market_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        setupRecyclerView()
        fetchMajorCategories()
    }

    private fun initViews() {
        btnMarket = findViewById(R.id.btnmarket)
        categoryContainer = findViewById(R.id.categoryContainer)
        recyclerView = findViewById(R.id.recyclerMarketList)
    }

    private fun setupListeners() {
        btnMarket.setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        val dummyList = listOf(
            "상품 1",
            "상품 2",
            "상품 3",
            "상품 4",
            "상품 5",
            "상품 6",
            "상품 7",
            "상품 8"
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MarketAdapter(dummyList)
    }

    // 🔥 카테고리 API 호출
    private fun fetchMajorCategories() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getMajorCategories()

                if (response.success) {
                    val categories = response.result.majorCategories.sortedBy { it.sortOrder }
                    renderCategoryButtons(categories)
                } else {
                    Toast.makeText(
                        this@MarketListActivity,
                        response.message.ifBlank { "카테고리 조회 실패" },
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("CATEGORY_API", "카테고리 불러오기 실패", e)
                Toast.makeText(
                    this@MarketListActivity,
                    "카테고리를 불러오지 못했어요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 🔥 버튼 생성 + 추가 (초기에는 전부 흰색)
    private fun renderCategoryButtons(categories: List<MajorCategory>) {
        categoryContainer.removeAllViews()
        selectedCategoryButton = null
        selectedCategoryId = null

        categories.forEach { category ->
            val button = createCategoryButton(category.name)

            button.setOnClickListener {
                updateSelectedButton(button)
                selectedCategoryId = category.majorCategoryId

                Log.d(
                    "CATEGORY_CLICK",
                    "선택한 카테고리: id=${category.majorCategoryId}, name=${category.name}"
                )

                // 👉 여기서 상품 API 연결하면 됨
                // fetchProducts(category.majorCategoryId)
            }

            categoryContainer.addView(button)
        }
    }

    // 🔥 버튼 스타일 (기본 = 흰색)
    private fun createCategoryButton(categoryName: String): Button {
        return Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(36)
            ).apply {
                marginEnd = dpToPx(8)
            }

            text = categoryName
            gravity = Gravity.CENTER
            textSize = 13f
            isAllCaps = false
            minWidth = 0
            minHeight = 0
            setPadding(dpToPx(16), 0, dpToPx(16), 0)

            // ✅ 기본은 전부 흰색
            setBackgroundResource(R.drawable.bg_chip_white)
            setTextColor(android.graphics.Color.parseColor("#666666"))
        }
    }

    // 🔥 선택 상태 변경
    private fun updateSelectedButton(button: Button) {

        // 이전 버튼 → 흰색으로 복구
        selectedCategoryButton?.let { prev ->
            prev.setBackgroundResource(R.drawable.bg_chip_white)
            prev.setTextColor(android.graphics.Color.parseColor("#666666"))
        }

        // 현재 버튼 → 선택 상태 (회색)
        button.setBackgroundResource(R.drawable.bg_chip_selected)
        button.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))

        selectedCategoryButton = button
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}