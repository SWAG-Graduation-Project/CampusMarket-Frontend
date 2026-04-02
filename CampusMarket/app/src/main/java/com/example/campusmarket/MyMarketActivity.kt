package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusmarket.data.model.MyStoreLatestProduct
import kotlinx.coroutines.launch

class MyMarketActivity : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var tvStoreTitle: TextView
    private lateinit var tvOpenDate: TextView
    private lateinit var tvTradeCount: TextView
    private lateinit var btnSelling: Button
    private lateinit var btnCompleted: Button
    private lateinit var btnSell: Button
    private lateinit var recyclerUserMarketPosts: RecyclerView
    private lateinit var profileCircle: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_market)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()
        setupViews()
        setupRecyclerView()
        loadMyStore()
    }

    private fun bindViews() {
        backButton = findViewById(R.id.backbutton)
        tvStoreTitle = findViewById(R.id.tvStoreTitle)
        tvOpenDate = findViewById(R.id.tvOpenDate)
        tvTradeCount = findViewById(R.id.tvTradeCount)
        btnSelling = findViewById(R.id.btn_selling)
        btnCompleted = findViewById(R.id.btn_completed)
        btnSell = findViewById(R.id.btn_sell)
        recyclerUserMarketPosts = findViewById(R.id.recyclerUserMarketPosts)
        profileCircle = findViewById(R.id.profileCircle)
    }

    private fun setupViews() {
        backButton.setOnClickListener {
            finish()
        }

        btnSell.setOnClickListener {
            startActivity(Intent(this, SellActivity::class.java))
        }

        btnSelling.setOnClickListener {
            Toast.makeText(this, "판매중 상품 목록입니다.", Toast.LENGTH_SHORT).show()
        }

        btnCompleted.setOnClickListener {
            Toast.makeText(this, "구매 완료 목록 API가 아직 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        recyclerUserMarketPosts.layoutManager = LinearLayoutManager(this)
        recyclerUserMarketPosts.adapter = MyMarketPostAdapter(emptyList())
    }

    private fun loadMyStore() {
        val guestUuid = GuestManager.getGuestUuid(this)

        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "게스트 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            showDefaultProfileImage()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("MY_STORE", "========== 내 상점 요청 시작 ==========")
                Log.d("MY_STORE", "BASE_URL = http://3.36.120.78:8080/api/")
                Log.d("MY_STORE", "endpoint = my-store")
                Log.d("MY_STORE", "header X-Guest-UUID = $guestUuid")
                Log.d("MY_STORE", "fullUrl 예상 = http://3.36.120.78:8080/api/my-store")

                val response = RetrofitClient.apiService.getMyStore(guestUuid)

                Log.d("MY_STORE", "response.code = ${response.code()}")
                Log.d("MY_STORE", "response.message = ${response.message()}")
                Log.d("MY_STORE", "response.isSuccessful = ${response.isSuccessful}")

                val errorText = response.errorBody()?.string()
                if (!errorText.isNullOrBlank()) {
                    Log.e("MY_STORE", "errorBody = $errorText")
                }

                if (!response.isSuccessful) {
                    showDefaultProfileImage()

                    when (response.code()) {
                        404 -> {
                            Toast.makeText(
                                this@MyMarketActivity,
                                "my-store API 주소가 서버에 없거나 경로가 다릅니다. 백엔드 경로 확인이 필요합니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        400 -> {
                            Toast.makeText(
                                this@MyMarketActivity,
                                "요청 형식이 잘못되었습니다. 헤더 이름이나 guestUuid를 확인해주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        500 -> {
                            Toast.makeText(
                                this@MyMarketActivity,
                                "서버 내부 오류입니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            Toast.makeText(
                                this@MyMarketActivity,
                                "상점 정보를 불러오지 못했습니다. code=${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    return@launch
                }

                val body = response.body()
                Log.d("MY_STORE", "response.body = $body")

                val result = body?.result ?: body?.data

                if (result == null) {
                    Log.e("MY_STORE", "result/data가 null입니다.")
                    showDefaultProfileImage()
                    Toast.makeText(this@MyMarketActivity, "상점 데이터가 비어 있습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val nickname = result.nickname?.takeIf { it.isNotBlank() } ?: "이름없는"
                tvStoreTitle.text = "${nickname}네 상점"

                val saleCount = result.saleCount ?: 0
                val purchaseCount = result.purchaseCount ?: 0
                val tradeCount = saleCount + purchaseCount
                tvTradeCount.text = "거래 횟수: ${tradeCount} 번"

                tvOpenDate.visibility = View.GONE

                loadProfileImage(result.profileImageUrl)

                val products: List<MyStoreLatestProduct> = result.latestProducts
                recyclerUserMarketPosts.adapter = MyMarketPostAdapter(products)

                Log.d("MY_STORE", "nickname = ${result.nickname}")
                Log.d("MY_STORE", "profileImageUrl = ${result.profileImageUrl}")
                Log.d("MY_STORE", "products.size = ${products.size}")
                Log.d("MY_STORE", "========== 내 상점 요청 성공 ==========")

            } catch (e: Exception) {
                Log.e("MY_STORE", "내 상점 조회 예외: ${e.message}", e)
                showDefaultProfileImage()
                Toast.makeText(this@MyMarketActivity, "내 상점 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfileImage(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            showDefaultProfileImage()
            return
        }

        Glide.with(this)
            .load(imageUrl)
            .placeholder(android.R.drawable.sym_def_app_icon)
            .error(android.R.drawable.sym_def_app_icon)
            .fallback(android.R.drawable.sym_def_app_icon)
            .circleCrop()
            .into(profileCircle)
    }

    private fun showDefaultProfileImage() {
        Glide.with(this)
            .load(android.R.drawable.sym_def_app_icon)
            .circleCrop()
            .into(profileCircle)
    }
}