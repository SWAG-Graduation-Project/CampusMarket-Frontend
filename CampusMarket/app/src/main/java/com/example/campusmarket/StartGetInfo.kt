package com.example.campusmarket

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.LockerDataSource
import com.example.campusmarket.network.RetrofitClient
import kotlinx.coroutines.launch

class StartGetInfo : AppCompatActivity() {

    private lateinit var etNickname: EditText
    private lateinit var btnRandom: Button
    private lateinit var btnCheck: Button
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var webView: WebView

    private val USE_MEMBER_MOCK = false
    private var isNicknameChecked = false

    private var currentBuildingName: String = ""
    private var currentFloor: Int = 1
    private var currentImageIndex: Int = 1

    private var selectedLockerGroup: SelectedLockerGroup? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_get_info)

        etNickname = findViewById(R.id.etNickname)
        btnRandom = findViewById(R.id.btnRandom)
        btnCheck = findViewById(R.id.btnCheck)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)
        webView = findViewById(R.id.webViewMap)

        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isNicknameChecked = false
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnRandom.setOnClickListener {
            getRandomNickname()
        }

        btnCheck.setOnClickListener {
            val nickname = etNickname.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkNickname(nickname)
        }

        btnNext.setOnClickListener {
            val nickname = etNickname.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isNicknameChecked) {
                Toast.makeText(this, "닉네임 중복 확인을 해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            goToMarket()
        }

        tvSkip.setOnClickListener {
            goToMarket()
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.clearCache(true)
        webView.clearHistory()

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(WebAppInterface(), "AndroidBridge")
        webView.loadUrl("https://map-web-sigma.vercel.app/")
    }

    private fun goToMarket() {
        val intent = Intent(this, MarketActivity::class.java)
        startActivity(intent)
        finish()
    }



    private fun getRandomNickname() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApi.getRandomNickname()

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        val nickname = body.result.nickname
                        etNickname.setText(nickname)
                        isNicknameChecked = false
                        Log.d("API", "닉네임 성공: $nickname")
                    } else {
                        Toast.makeText(this@StartGetInfo, body?.message ?: "닉네임 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@StartGetInfo, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "닉네임 예외", e)
                Toast.makeText(this@StartGetInfo, "연결 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkNickname(nickname: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApi.checkNickname(nickname)

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success) {
                        if (body.result.available) {
                            isNicknameChecked = true
                            Toast.makeText(
                                this@StartGetInfo,
                                "사용 가능한 닉네임입니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            isNicknameChecked = false
                            Toast.makeText(
                                this@StartGetInfo,
                                "이미 사용 중인 닉네임입니다",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        isNicknameChecked = false
                        Toast.makeText(
                            this@StartGetInfo,
                            body?.message ?: "닉네임 확인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    isNicknameChecked = false
                    Toast.makeText(this@StartGetInfo, "서버 오류", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                isNicknameChecked = false
                Log.e("API", "닉네임 체크 예외", e)
                Toast.makeText(this@StartGetInfo, "연결 실패", Toast.LENGTH_SHORT).show()
            }
        }
    }
    inner class WebAppInterface {
        @JavascriptInterface
        fun onBuildingSelected(buildingName: String) {
            runOnUiThread {
                showFloorDialog(buildingName)
            }
        }
    }

    private fun showFloorDialog(buildingName: String) {
        currentBuildingName = buildingName

        val dialog = FloorSelectDialogFragment(buildingName) { selectedFloor ->
            currentFloor = selectedFloor

            when (buildingName) {
                "차관" -> {
                    Toast.makeText(this, "차관 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                    openLockerGroupPopup(buildingName, selectedFloor, 1)
                }

                "인문대" -> {
                    Toast.makeText(this, "인문대 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }

                "자연대" -> {
                    Toast.makeText(this, "자연대 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }

                "예술대학" -> {
                    Toast.makeText(this, "예술대학 ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    Toast.makeText(this, "$buildingName ${selectedFloor}층 선택", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show(supportFragmentManager, "FloorSelectDialog")
    }

    private fun openLockerGroupPopup(buildingName: String, floor: Int, imageIndex: Int) {
        currentImageIndex = imageIndex

        val popup = LockerGroupPopupDialogFragment(
            buildingName = buildingName,
            floor = floor,
            imageIndex = imageIndex,
            loungeImages = LockerDataSource.loungeImageList,
            lockerGroups = LockerDataSource.lockerList
        ) { selected ->
            selectedLockerGroup = selected

            Log.d(
                "LOCKER",
                "선택 저장: building=${selected.buildingName}, floor=${selected.floor}, major=${selected.major}, group=${selected.groupNumber}"
            )

            Toast.makeText(
                this,
                "${selected.major} ${selected.groupNumber}번 그룹 저장 완료",
                Toast.LENGTH_SHORT
            ).show()
        }

        popup.show(supportFragmentManager, "LockerGroupPopup")
    }
}