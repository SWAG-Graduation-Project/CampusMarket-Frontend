package com.example.campusmarket

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MarketActivity : AppCompatActivity() {

    private lateinit var categoryContainer: LinearLayout
    private lateinit var btnAll: Button
    private lateinit var shopSigns: List<TextView>
    private lateinit var shopImages: List<ImageView>

    private var selectedMajorCategoryId: Long? = null

    private val apiService by lazy { RetrofitClient.apiService }

    private val apiBaseUrl = "http://3.36.120.78:8080"

    // 테스트용 상품 ID
    private val testProductIds = listOf(22L, 23L, 24L, 25L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_market)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupTopButtons()
        setupBottomNavigation()

        loadMajorCategories()
        loadStores()

        // 테스트용: 22~25번 상품의 displayAssetImageUrl 띄우기
        loadTestProductImages()
    }

    private fun bindViews() {
        categoryContainer = findViewById(R.id.categoryContainer)
        btnAll = findViewById(R.id.btnAll)

        shopSigns = listOf(
            findViewById(R.id.tvShopSign1),
            findViewById(R.id.tvShopSign2),
            findViewById(R.id.tvShopSign3),
            findViewById(R.id.tvShopSign4),
            findViewById(R.id.tvShopSign5),
            findViewById(R.id.tvShopSign6),
            findViewById(R.id.tvShopSign7),
            findViewById(R.id.tvShopSign8),
            findViewById(R.id.tvShopSign9),
            findViewById(R.id.tvShopSign10),
            findViewById(R.id.tvShopSign11),
            findViewById(R.id.tvShopSign12)
        )

        shopImages = listOf(
            findViewById(R.id.ivShopItem1),
            findViewById(R.id.ivShopItem2),
            findViewById(R.id.ivShopItem3),
            findViewById(R.id.ivShopItem4),
            findViewById(R.id.ivShopItem5),
            findViewById(R.id.ivShopItem6),
            findViewById(R.id.ivShopItem7),
            findViewById(R.id.ivShopItem8),
            findViewById(R.id.ivShopItem9),
            findViewById(R.id.ivShopItem10),
            findViewById(R.id.ivShopItem11),
            findViewById(R.id.ivShopItem12)
        )
    }

    private fun setupTopButtons() {
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

        btnAll.setOnClickListener {
            selectCategoryButton(btnAll)
            selectedMajorCategoryId = null
            loadStores()
        }
    }

    private fun setupBottomNavigation() {
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
                    loadStores()
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
                val nickname = if (store.nickname.isBlank()) "이름없는" else store.nickname
                shopSigns[i].text = "${nickname}이네 상점"
            } else {
                shopSigns[i].text = "빈 상점"
            }
        }
    }

    private fun loadTestProductImages() {
        lifecycleScope.launch {
            try {
                val memberId = GuestManager.getMemberId(this@MarketActivity) ?: 1L

                for (i in shopImages.indices) {
                    shopImages[i].setImageDrawable(null)
                }

                val jobs = testProductIds.mapIndexed { index, productId ->
                    async {
                        try {
                            Log.d("TEST_PRODUCT_IMAGE", "요청 productId=$productId")

                            val response = apiService.getProductDetail(
                                productId = productId,
                                memberId = memberId
                            )

                            val product = response.result
                            if (product == null) {
                                Log.e(
                                    "TEST_PRODUCT_IMAGE",
                                    "result null productId=$productId, code=${response.code}, message=${response.message}"
                                )
                                return@async
                            }

                            val rawPath = product.displayAssetImageUrl
                            val finalUrl = normalizeImageUrl(rawPath)

                            Log.d(
                                "TEST_PRODUCT_IMAGE",
                                "productId=$productId, rawPath=$rawPath, finalUrl=$finalUrl"
                            )

                            if (finalUrl != null) {
                                val bitmap = loadBitmapFromUrl(finalUrl)
                                withContext(Dispatchers.Main) {
                                    if (bitmap != null && index < shopImages.size) {
                                        shopImages[index].setImageBitmap(bitmap)
                                        shopSigns[index].text = "ID $productId"
                                    } else if (index < shopSigns.size) {
                                        shopSigns[index].text = "ID $productId (이미지 없음)"
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    if (index < shopSigns.size) {
                                        shopSigns[index].text = "ID $productId (URL 없음)"
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("TEST_PRODUCT_IMAGE", "상품 상세 조회 실패 productId=$productId", e)
                            withContext(Dispatchers.Main) {
                                if (index < shopSigns.size) {
                                    shopSigns[index].text = "ID $productId 실패"
                                }
                            }
                        }
                    }
                }

                jobs.awaitAll()
            } catch (e: Exception) {
                Log.e("TEST_PRODUCT_IMAGE", "테스트 이미지 전체 로딩 실패", e)
            }
        }
    }

    private fun normalizeImageUrl(rawPath: String?): String? {
        if (rawPath.isNullOrBlank()) return null

        return when {
            rawPath.startsWith("http://") || rawPath.startsWith("https://") -> rawPath
            rawPath.startsWith("/") -> "$apiBaseUrl$rawPath"
            else -> "$apiBaseUrl/$rawPath"
        }
    }

    private suspend fun loadBitmapFromUrl(imageUrl: String) = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 10000
                doInput = true
                connect()
            }

            val responseCode = connection.responseCode
            Log.d("IMAGE_LOAD", "url=$imageUrl code=$responseCode")

            if (responseCode !in 200..299) {
                connection.disconnect()
                return@withContext null
            }

            val stream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(stream)
            stream.close()
            connection.disconnect()

            bitmap
        } catch (e: Exception) {
            Log.e("IMAGE_LOAD", "이미지 로드 실패: $imageUrl", e)
            null
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