package com.example.campusmarket

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.ParsedTimetable
import com.example.campusmarket.data.model.TimetableClass
import com.example.campusmarket.data.model.TimetableClassRequest
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

class MyTimeTableActivity : AppCompatActivity() {

    private lateinit var weeklyTimetableView: WeeklyTimetableView
    private lateinit var boxTimetableImage: FrameLayout
    private lateinit var btnUploadTimetable: Button
    private lateinit var btnEditTimetable: Button

    private val memberApi: MemberApi by lazy { RetrofitClient.memberApi }
    private val gson = Gson()

    private var currentTimetableData: String = ""
    private var currentNickname: String = ""
    private var currentProfileImageUrl: String = ""
    private var currentLockerName: String = ""

    private val pickTimetableImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) uploadTimetableImage(uri)
                else Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_time_table)

        bindViews()
        setupTimetableView()
        setupListeners()
        loadProfile()
    }

    private fun bindViews() {
        boxTimetableImage = findViewById(R.id.boxTimetableImage)
        btnUploadTimetable = findViewById(R.id.btnUploadTimetable)
        btnEditTimetable = findViewById(R.id.btnEditTimetable)

        // 뒤로가기 버튼
        findViewById<Button>(R.id.backbutton).setOnClickListener { finish() }

        // 하단 네비게이션
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
                startActivity(Intent(this, MypageActivity::class.java))
            }
        }
    }

    private fun setupTimetableView() {
        boxTimetableImage.removeAllViews()
        weeklyTimetableView = WeeklyTimetableView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        boxTimetableImage.addView(weeklyTimetableView)
    }

    private fun setupListeners() {
        btnUploadTimetable.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickTimetableImageLauncher.launch(intent)
        }

        btnEditTimetable.setOnClickListener {
            showClassListDialog()
        }
    }

    private fun loadProfile() {
        val guestUuid = GuestManager.getGuestUuid(this) ?: return

        lifecycleScope.launch {
            try {
                val response = memberApi.getMyProfile(guestUuid)
                if (response.isSuccessful) {
                    val data = response.body()?.result ?: return@launch
                    currentNickname = data.nickname ?: ""
                    currentProfileImageUrl = data.profileImageUrl ?: ""
                    currentLockerName = data.lockerName ?: ""
                    currentTimetableData = data.timetableData ?: ""

                    if (currentTimetableData.isNotBlank()) {
                        renderTimetable(currentTimetableData)
                    } else {
                        weeklyTimetableView.setTimetable(emptyList())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                weeklyTimetableView.setTimetable(emptyList())
            }
        }
    }

    private fun renderTimetable(json: String) {
        try {
            val parsed = gson.fromJson(json, ParsedTimetable::class.java)
            val classList = parsed.classes.filter { !it.name.isNullOrBlank() }
            weeklyTimetableView.setTimetable(classList)
        } catch (e: Exception) {
            weeklyTimetableView.setTimetable(emptyList())
        }
    }

    private fun uploadTimetableImage(uri: Uri) {
        val guestUuid = GuestManager.getGuestUuid(this) ?: return

        lifecycleScope.launch {
            try {
                val imageFile = withContext(Dispatchers.IO) { uriToFile(uri) }
                if (imageFile == null || !imageFile.exists()) {
                    Toast.makeText(this@MyTimeTableActivity, "이미지 파일 변환에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val requestFile = imageFile.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                val response = memberApi.parseTimetableImage(guestUuid, imagePart)
                if (!response.isSuccessful) {
                    Toast.makeText(this@MyTimeTableActivity, "업로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val rawElement = response.body()?.result?.timetableData
                val newTimetableData = when {
                    rawElement == null -> null
                    rawElement.isJsonPrimitive && rawElement.asJsonPrimitive.isString -> rawElement.asString
                    else -> rawElement.toString()
                }

                if (!newTimetableData.isNullOrBlank()) {
                    currentTimetableData = newTimetableData
                    renderTimetable(newTimetableData)
                    saveProfile(timetableData = newTimetableData)
                } else {
                    Toast.makeText(this@MyTimeTableActivity, "시간표 파싱 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MyTimeTableActivity, "시간표 업로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 수업 목록 다이얼로그 ────────────────────────────────────────────────

    private fun showClassListDialog() {
        if (currentTimetableData.isBlank()) {
            Toast.makeText(this, "등록된 시간표가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val parsed = try {
            gson.fromJson(currentTimetableData, ParsedTimetable::class.java)
        } catch (e: Exception) {
            Toast.makeText(this, "시간표 데이터를 읽을 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val classes = parsed.classes
        if (classes.isEmpty()) {
            Toast.makeText(this, "등록된 수업이 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = classes.mapIndexed { index, c ->
            val name = c.name ?: "(이름 없음)"
            "${index + 1}. $name  ${c.day} ${c.startTime}~${c.endTime}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("수정할 수업 선택")
            .setItems(labels) { _, which ->
                showClassEditDialog(which, classes[which])
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showClassEditDialog(classIndex: Int, cls: TimetableClass) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
        }

        val etName = EditText(this).apply {
            hint = "수업명"
            setText(cls.name ?: "")
        }
        val etDay = EditText(this).apply {
            hint = "요일 (월/화/수/목/금/토/일)"
            setText(cls.day)
        }
        val etStart = EditText(this).apply {
            hint = "시작 시간 (예: 09:00)"
            setText(cls.startTime)
        }
        val etEnd = EditText(this).apply {
            hint = "종료 시간 (예: 11:00)"
            setText(cls.endTime)
        }
        val etLocation = EditText(this).apply {
            hint = "강의실 (선택)"
            setText(cls.location ?: "")
        }

        layout.addView(etName)
        layout.addView(etDay)
        layout.addView(etStart)
        layout.addView(etEnd)
        layout.addView(etLocation)

        AlertDialog.Builder(this)
            .setTitle("수업 수정")
            .setView(layout)
            .setPositiveButton("저장") { _, _ ->
                val request = TimetableClassRequest(
                    name = etName.text.toString().trim().takeIf { it.isNotBlank() },
                    day = etDay.text.toString().trim(),
                    startTime = etStart.text.toString().trim(),
                    endTime = etEnd.text.toString().trim(),
                    location = etLocation.text.toString().trim().takeIf { it.isNotBlank() }
                )
                updateTimetableClass(classIndex, request)
            }
            .setNeutralButton("삭제") { _, _ ->
                confirmDeleteClass(classIndex)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun confirmDeleteClass(classIndex: Int) {
        AlertDialog.Builder(this)
            .setTitle("수업 삭제")
            .setMessage("이 수업을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                deleteTimetableClass(classIndex)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateTimetableClass(classIndex: Int, request: TimetableClassRequest) {
        val guestUuid = GuestManager.getGuestUuid(this) ?: return

        lifecycleScope.launch {
            try {
                val response = memberApi.updateTimetableClass(guestUuid, classIndex, request)
                if (response.isSuccessful) {
                    val newData = response.body()?.result?.timetableData
                    if (!newData.isNullOrBlank()) {
                        currentTimetableData = newData
                        renderTimetable(newData)
                        Toast.makeText(this@MyTimeTableActivity, "수업이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MyTimeTableActivity, "수정 완료 (응답 없음)", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@MyTimeTableActivity, "수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MyTimeTableActivity, "수정 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteTimetableClass(classIndex: Int) {
        val guestUuid = GuestManager.getGuestUuid(this) ?: return

        lifecycleScope.launch {
            try {
                val response = memberApi.deleteTimetableClass(guestUuid, classIndex)
                if (response.isSuccessful) {
                    val newData = response.body()?.result?.timetableData
                    if (!newData.isNullOrBlank()) {
                        currentTimetableData = newData
                        renderTimetable(newData)
                    } else {
                        weeklyTimetableView.setTimetable(emptyList())
                    }
                    Toast.makeText(this@MyTimeTableActivity, "수업이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MyTimeTableActivity, "삭제 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MyTimeTableActivity, "삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile(timetableData: String) {
        val guestUuid = GuestManager.getGuestUuid(this) ?: return

        val request = ProfileInitRequest(
            nickname = currentNickname,
            profileImageUrl = currentProfileImageUrl,
            lockerName = currentLockerName,
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
                    }
                    Toast.makeText(this@MyTimeTableActivity, "시간표가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MyTimeTableActivity, "저장 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MyTimeTableActivity, "저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
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
