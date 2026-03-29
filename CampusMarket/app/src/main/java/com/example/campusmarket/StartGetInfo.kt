package com.example.campusmarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

data class LockerData(
    val buildingName: String,
    val floor: Int,
    val imageIndex: Int,
    val originalX: Float,
    val originalY: Float,
    val major: String,
    val groupNumber: Int,
    val lockerImageResId: Int,
    val offsetX: Float = 40f,
    val offsetY: Float = 120f
)

data class LoungeImageData(
    val buildingName: String,
    val floor: Int,
    val imageIndex: Int,
    val imageResId: Int
)

class StartGetInfo : AppCompatActivity() {

    private lateinit var webView: WebView

    private lateinit var loungeOverlay: FrameLayout
    private lateinit var loungeContainer: FrameLayout
    private lateinit var loungeImageStage: FrameLayout
    private lateinit var imgLounge: ImageView
    private lateinit var btnCloseLounge: ImageButton

    private var currentBuildingName: String = ""
    private var currentFloor: Int = 1
    private var currentImageIndex: Int = 1

    // 배경 이미지 목록
    private val loungeImageList = listOf(
        LoungeImageData("차관", 1, 1, R.drawable.cha_center1),
        LoungeImageData("차관", 2, 1, R.drawable.cha_center2),
        LoungeImageData("차관", 3, 1, R.drawable.cha_center3),
        LoungeImageData("차관", 4, 1, R.drawable.cha_center4)

        // 필요하면 계속 추가
        // LoungeImageData("차관", 2, 1, R.drawable.cha_2f_1),
        // LoungeImageData("인문대", 1, 1, R.drawable.human_1f_1)
    )

    // 사물함 목록
    private val lockerList = listOf(
        LockerData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 637f,
            originalY = 915f,
            major = "경영",
            groupNumber = 1,
            lockerImageResId = R.drawable.lockerback1,
            offsetX = 45f,
            offsetY = 120f
        ),

        // 예시 추가
        LockerData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 795f,
            originalY = 907f,
            major = "경영",
            groupNumber = 2,
            lockerImageResId = R.drawable.lockerback2,
            offsetX = 45f,
            offsetY = 120f
        ),

        LockerData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 921f,
            originalY = 808f,
            major = "경영",
            groupNumber = 3,
            lockerImageResId = R.drawable.lockerback1,
            offsetX = 45f,
            offsetY = 120f
        ),
                LockerData(
                buildingName = "차관",
        floor = 1,
        imageIndex = 1,
        originalX = 1086f,
        originalY = 717f,
        major = "경영",
        groupNumber = 4,
        lockerImageResId = R.drawable.lockerback1,
                    offsetX = 45f,
                    offsetY = 120f
    ),
        LockerData(
            buildingName = "차관",
            floor = 1,
            imageIndex = 1,
            originalX = 1618f,
            originalY = 349f,
            major = "미상",
            groupNumber = 3,
            lockerImageResId = R.drawable.lockerfrontleft,
            offsetX = 45f,
            offsetY = 120f
        ),
        // 필요하면 건물/층/이미지번호별로 계속 추가
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_get_info)

        webView = findViewById(R.id.webViewMap)

        loungeOverlay = findViewById(R.id.loungeOverlay)
        loungeContainer = findViewById(R.id.loungeContainer)
        loungeImageStage = findViewById(R.id.loungeImageStage)
        imgLounge = findViewById(R.id.imgLounge)
        btnCloseLounge = findViewById(R.id.btnCloseLounge)

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

        btnCloseLounge.setOnClickListener {
            hideLoungeImage()
        }

        loungeOverlay.setOnClickListener {
            hideLoungeImage()
        }

        loungeContainer.setOnClickListener {
            // 내부 클릭 시 닫히지 않게
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
                    showLoungeImage(selectedFloor, 1)
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
    private fun showLoungeImage(floor: Int, imageIndex: Int) {
        currentFloor = floor
        currentImageIndex = imageIndex

        val loungeImage = loungeImageList.find {
            it.buildingName == currentBuildingName &&
                    it.floor == floor &&
                    it.imageIndex == imageIndex
        }

        if (loungeImage == null) {
            Toast.makeText(
                this,
                "배경 이미지가 없습니다: $currentBuildingName ${floor}층 ${imageIndex}번",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        imgLounge.setImageResource(loungeImage.imageResId)
        loungeOverlay.visibility = View.VISIBLE

        loungeImageStage.post {
            removeLockerViews()

            val currentLockers = lockerList.filter {
                it.buildingName == currentBuildingName &&
                        it.floor == floor &&
                        it.imageIndex == imageIndex
            }

            addLockers(currentLockers)
        }
    }

    private fun addLockers(lockers: List<LockerData>) {
        for (lockerData in lockers) {
            addLocker(lockerData)
        }
    }

    private fun addLocker(lockerData: LockerData) {
        val (displayX, displayY) = convertToDisplayPosition(
            lockerData.originalX,
            lockerData.originalY
        )

        val locker = ImageView(this)
        locker.setImageResource(lockerData.lockerImageResId)

        val lockerWidth = (loungeImageStage.width * 0.08f).toInt()
        val lockerHeight = (lockerWidth * 1.2f).toInt()

        val params = FrameLayout.LayoutParams(lockerWidth, lockerHeight)
        locker.layoutParams = params

        val (finalX, finalY) = applyLockerOffset(
            displayX = displayX,
            displayY = displayY,
            lockerWidth = lockerWidth,
            lockerHeight = lockerHeight,
            offsetX = lockerData.offsetX,
            offsetY = lockerData.offsetY
        )

        locker.x = finalX
        locker.y = finalY

        locker.setOnClickListener {

            // 1. 기존 선택 상태 초기화 (다른 사물함 원래대로)
            resetLockerSelection()

            // 2. 선택된 사물함 확대 + 그림자
            locker.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150)
                .start()

            locker.elevation = 20f

            Toast.makeText(
                this,
                "${lockerData.major} / ${lockerData.groupNumber}번 그룹",
                Toast.LENGTH_SHORT
            ).show()
        }

        loungeImageStage.addView(locker)
    }
    private fun resetLockerSelection() {
        for (i in 0 until loungeImageStage.childCount) {
            val child = loungeImageStage.getChildAt(i)

            if (child is ImageView && child.id != R.id.imgLounge) {
                child.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()

                child.elevation = 0f
            }
        }
    }
    private fun convertToDisplayPosition(
        originalX: Float,
        originalY: Float
    ): Pair<Float, Float> {
        val originalWidth = 2000f
        val originalHeight = 1500f

        val stageWidth = loungeImageStage.width.toFloat()
        val stageHeight = loungeImageStage.height.toFloat()

        val displayX = (originalX / originalWidth) * stageWidth
        val displayY = (originalY / originalHeight) * stageHeight

        return Pair(displayX, displayY)
    }

    private fun applyLockerOffset(
        displayX: Float,
        displayY: Float,
        lockerWidth: Int,
        lockerHeight: Int,
        offsetX: Float,
        offsetY: Float
    ): Pair<Float, Float> {
        val finalX = displayX - lockerWidth / 2f + offsetX
        val finalY = displayY - lockerHeight / 2f + offsetY
        return Pair(finalX, finalY)
    }

    private fun removeLockerViews() {
        for (i in loungeImageStage.childCount - 1 downTo 0) {
            val child = loungeImageStage.getChildAt(i)
            if (child.id != R.id.imgLounge) {
                loungeImageStage.removeViewAt(i)
            }
        }
    }

    private fun hideLoungeImage() {
        loungeOverlay.visibility = View.GONE
    }
}