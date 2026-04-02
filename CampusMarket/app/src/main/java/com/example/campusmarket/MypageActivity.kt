package com.example.campusmarket

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.campusmarket.data.model.ParsedTimetable
import com.example.campusmarket.data.model.TimetableClass
import com.example.campusmarket.model.ProfileInitRequest
import com.example.campusmarket.network.MemberApi
import com.example.campusmarket.ui.view.WeeklyTimetableView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class MypageActivity : AppCompatActivity() {

    private lateinit var btnEditTimetable: Button
    private lateinit var btnEditLocker: Button
    private lateinit var profileNameButton: Button
    private lateinit var tvLockerName: TextView
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var timetableContainer: FrameLayout
    private lateinit var weeklyTimetableView: WeeklyTimetableView

    private var currentNickname: String = ""
    private var currentProfileImageUrl: String = ""
    private var currentLockerName: String = ""
    private var currentTimetableData: String = ""

    private val memberApi: MemberApi by lazy { RetrofitClient.memberApi }

    private val pickProfileImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) uploadProfileImage(uri)
                else Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        bindViews()
        setupTimetableContainer()
        setupListeners()
        fetchMyProfile()
    }

    private fun bindViews() {
        btnEditTimetable = findViewById(R.id.btnEditTimetable)
        btnEditLocker = findViewById(R.id.btnEditLocker)
        profileNameButton = findViewById(R.id.profilename)
        tvLockerName = findViewById(R.id.tvLockerName)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)
        timetableContainer = findViewById(R.id.boxTimetableImage)
    }

    private fun setupTimetableContainer() {
        timetableContainer.removeAllViews()
        weeklyTimetableView = WeeklyTimetableView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        timetableContainer.addView(weeklyTimetableView)
    }

    private fun setupListeners() {
        btnEditTimetable.setOnClickListener {
            startActivity(Intent(this, MyTimeTableActivity::class.java))
        }

        btnEditLocker.setOnClickListener {
            startActivity(Intent(this, StartGetInfo::class.java))
        }

        profileNameButton.setOnClickListener {
            showNicknameEditDialog()
        }

        // 프로필 사진 클릭 → 이미지 선택
        ivProfilePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickProfileImageLauncher.launch(intent)
        }

        // 찜 목록
        val btnWishlist = findViewById<Button>(R.id.btnWishlist)
        btnWishlist.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        // 하단 네비게이션
        setupBottomNavigation()

        // 회원탈퇴
        setupWithdrawalButton()
    }

    private fun setupBottomNavigation() {
        // 네비게이션 바 LinearLayout 안의 자식 뷰로 클릭 처리
        // (activity_mypage.xml NavBar 내 id가 없어서 tag 또는 position 기반으로 처리)
        val navBar = findViewById<LinearLayout>(R.id.NavBar)
        if (navBar != null && navBar.childCount >= 4) {
            navBar.getChildAt(0).setOnClickListener {
                startActivity(Intent(this, MarketActivity::class.java))
            }
            navBar.getChildAt(1).setOnClickListener {
                startActivity(Intent(this, MyMarketActivity::class.java))
            }
            navBar.getChildAt(2).setOnClickListener {
                startActivity(Intent(this, ChatListActivity::class.java))
            }
            navBar.getChildAt(3).setOnClickListener {
                // 현재 페이지
            }
        }
    }

    private fun setupWithdrawalButton() {
        // 회원탈퇴 버튼이 레이아웃에 있으면 연결, 없으면 ProfileName 옆에 작은 텍스트로 추가
        val btnWithdraw = findViewById<View>(R.id.btnWithdraw)
        btnWithdraw?.setOnClickListener {
            showWithdrawalDialog()
        }
    }

    private fun showNicknameEditDialog() {
        val editText = EditText(this).apply {
            setText(currentNickname)
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle("닉네임 수정")
            .setView(editText)
            .setPositiveButton("저장") { _, _ ->
                val newNickname = editText.text.toString().trim()
                if (newNickname.isNotBlank()) {
                    saveProfile(nickname = newNickname)
                } else {
                    Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showWithdrawalDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말 탈퇴하시겠습니까?\n탈퇴 후에는 되돌릴 수 없습니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                withdrawMember()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun withdrawMember() {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = memberApi.withdrawMember(guestUuid)
                if (response.isSuccessful) {
                    // 로컬 UUID 삭제
                    GuestManager.clearGuestData(this@MypageActivity)
                    Toast.makeText(this@MypageActivity, "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MypageActivity, StartActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } else {
                    Toast.makeText(this@MypageActivity, "탈퇴 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MypageActivity, "탈퇴 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchMyProfile() {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            showEmptyTimetable()
            return
        }

        lifecycleScope.launch {
            try {
                val response = memberApi.getMyProfile(guestUuid)
                if (!response.isSuccessful || response.body()?.result == null) {
                    showEmptyTimetable()
                    return@launch
                }

                val data = response.body()!!.result!!
                currentNickname = data.nickname ?: ""
                currentProfileImageUrl = data.profileImageUrl ?: ""
                currentLockerName = data.lockerName ?: ""
                currentTimetableData = data.timetableData ?: ""

                profileNameButton.text = currentNickname.ifBlank { "닉네임 없음" }
                tvLockerName.text = if (currentLockerName.isBlank()) "• 등록된 사물함이 없습니다." else "• $currentLockerName"

                // 프로필 이미지 로드
                if (currentProfileImageUrl.isNotBlank()) {
                    Glide.with(this@MypageActivity)
                        .load(currentProfileImageUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_myinfo)
                        .into(ivProfilePhoto)
                }

                if (currentTimetableData.isNotBlank()) {
                    renderTimetableFromJson(currentTimetableData)
                } else {
                    showEmptyTimetable()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                showEmptyTimetable()
            }
        }
    }

    private fun saveProfile(
        nickname: String = currentNickname,
        profileImageUrl: String = currentProfileImageUrl,
        lockerName: String = currentLockerName,
        timetableData: String = currentTimetableData
    ) {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProfileInitRequest(
            nickname = nickname,
            profileImageUrl = profileImageUrl,
            lockerName = lockerName,
            timetableData = timetableData
        )

        lifecycleScope.launch {
            try {
                val response = memberApi.updateProfile(guestUuid, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val result = response.body()!!.result
                    if (result != null) {
                        currentNickname = result.nickname
                        currentProfileImageUrl = result.profileImageUrl
                        currentLockerName = result.lockerName
                        currentTimetableData = result.timetableData

                        profileNameButton.text = currentNickname.ifBlank { "닉네임 없음" }
                        tvLockerName.text = if (currentLockerName.isBlank()) "• 등록된 사물함이 없습니다." else "• $currentLockerName"
                    }
                    Toast.makeText(this@MypageActivity, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MypageActivity, "저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MypageActivity, "저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 프로필 이미지 업로드 ────────────────────────────────────────────────

    private fun uploadProfileImage(uri: Uri) {
        val guestUuid = GuestManager.getGuestUuid(this)
        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val imageFile = withContext(Dispatchers.IO) { uriToFile(uri) }
                if (imageFile == null || !imageFile.exists()) {
                    Toast.makeText(this@MypageActivity, "이미지 파일 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = imageFile.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestBody)

                val response = RetrofitClient.apiService.uploadProfileImage(guestUuid, imagePart)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.result?.imageUrl ?: return@launch
                    currentProfileImageUrl = imageUrl
                    // S3 버킷 비공개로 Glide URL 로딩이 403 → 로컬 URI로 즉시 표시
                    Glide.with(this@MypageActivity)
                        .load(uri)
                        .circleCrop()
                        .into(ivProfilePhoto)
                    saveProfile(profileImageUrl = imageUrl)
                } else {
                    Toast.makeText(this@MypageActivity, "프로필 이미지 업로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MypageActivity, "프로필 이미지 업로드 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 시간표 이미지 업로드 ────────────────────────────────────────────────

    private fun renderTimetableFromJson(json: String) {
        try {
            val parsedTimetable = Gson().fromJson(json, ParsedTimetable::class.java)
            val classList = parsedTimetable.classes.filter { !it.name.isNullOrBlank() }
            if (classList.isEmpty()) showEmptyTimetable() else showTimetable(classList)
        } catch (e: Exception) {
            showEmptyTimetable()
        }
    }

    private fun showTimetable(classList: List<TimetableClass>) {
        timetableContainer.visibility = View.VISIBLE
        weeklyTimetableView.visibility = View.VISIBLE
        weeklyTimetableView.setTimetable(classList)
    }

    private fun showEmptyTimetable() {
        timetableContainer.visibility = View.VISIBLE
        weeklyTimetableView.visibility = View.VISIBLE
        weeklyTimetableView.setTimetable(emptyList())
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = getFileName(uri) ?: "image_${System.currentTimeMillis()}.jpg"
            val tempFile = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && index >= 0) {
                    name = cursor.getString(index)
                }
            }
        }
        if (name == null) name = uri.lastPathSegment
        return name
    }
}

