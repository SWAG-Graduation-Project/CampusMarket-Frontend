package com.example.campusmarket

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.MajorCategory
import com.example.campusmarket.data.model.Store
import com.example.campusmarket.RetrofitClient
import kotlinx.coroutines.launch

class MarketActivity : AppCompatActivity() {

    private lateinit var categoryContainer: LinearLayout
    private lateinit var btnAll: Button
    private lateinit var shopSigns: List<TextView>

    private var selectedMajorCategoryId: Long? = null

    private val apiService by lazy { RetrofitClient.apiService }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_market)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        categoryContainer = findViewById(R.id.categoryContainer)
        btnAll = findViewById(R.id.btnAll)

        // 판자 TextView 9개
        shopSigns = listOf(
            findViewById(R.id.tvShopSign1),
            findViewById(R.id.tvShopSign2),
            findViewById(R.id.tvShopSign3),
            findViewById(R.id.tvShopSign4),
            findViewById(R.id.tvShopSign5),
            findViewById(R.id.tvShopSign6),
            findViewById(R.id.tvShopSign7),
            findViewById(R.id.tvShopSign8),
            findViewById(R.id.tvShopSign9)
        )

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
        btnList.setOnClickListener {
            startActivity(Intent(this, MarketListActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.gohome).setOnClickListener {
            startActivity(Intent(this, MarketActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.goMymarket).setOnClickListener {
            startActivity(Intent(this, MyMarketActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.gomypage).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.gochat).setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }

        btnAll.setOnClickListener {
            selectCategoryButton(btnAll)
            selectedMajorCategoryId = null
        }

        loadMajorCategories()
        loadStores()
    }

    private fun loadMajorCategories() {
        lifecycleScope.launch {
            try {
                val response = apiService.getMajorCategories()
                val categories = response.result.majorCategories.sortedBy { it.sortOrder }
                addCategoryButtons(categories)
            } catch (e: Exception) {
                Log.e("CATEGORY_API", "카테고리 조회 실패", e)
            }
        }
    }

    private fun addCategoryButtons(categories: List<MajorCategory>) {
        categories.forEach { category ->
            val button = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dpToPx(36)
                ).apply {
                    marginEnd = dpToPx(8)
                }

                minWidth = 0
                minHeight = 0
                setPadding(dpToPx(16), 0, dpToPx(16), 0)

                text = category.name
                isAllCaps = false
                textSize = 13f
                setTextColor(ContextCompat.getColor(context, R.color.chip_text_default))
                background = ContextCompat.getDrawable(context, R.drawable.bg_chip_white)

                setOnClickListener {
                    selectCategoryButton(this)
                    selectedMajorCategoryId = category.majorCategoryId
                }
            }

            categoryContainer.addView(button)
        }
    }

    private fun loadStores(page: Int = 0, size: Int = 12) {
        lifecycleScope.launch {
            try {
                val response = apiService.getStores(page, size)
                val stores = response.result.stores

                Log.d("STORE_API", "상점 개수: ${stores.size}")
                updateStoreTables(stores)

            } catch (e: Exception) {
                Log.e("STORE_API", "상점 조회 실패", e)
            }
        }
    }

    private fun updateStoreTables(stores: List<Store>) {
        for (i in shopSigns.indices) {
            if (i < stores.size) {
                val store = stores[i]
                shopSigns[i].text = "${store.nickname}이네 상점"
            } else {
                shopSigns[i].text = "빈 상점"
            }
        }
    }

    private fun selectCategoryButton(selectedButton: Button) {
        for (i in 0 until categoryContainer.childCount) {
            val child = categoryContainer.getChildAt(i)
            if (child is Button) {
                child.background = ContextCompat.getDrawable(this, R.drawable.bg_chip_white)
                child.setTextColor(
                    ContextCompat.getColor(this, R.color.chip_text_default)
                )
            }
        }

        selectedButton.background =
            ContextCompat.getDrawable(this, R.drawable.bg_chip_selected)
        selectedButton.setTextColor(
            ContextCompat.getColor(this, android.R.color.white)
        )
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun showMarketPopup() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_market_detail)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        val window = dialog.window ?: return
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.y = 0
        window.attributes = params

        window.setDimAmount(0.6f)
    }

}