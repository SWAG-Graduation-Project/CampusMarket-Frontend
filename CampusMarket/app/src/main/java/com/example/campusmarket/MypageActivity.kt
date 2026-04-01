package com.example.campusmarket

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class MypageActivity : AppCompatActivity() {

    private lateinit var btnEditTimetable: Button
    private lateinit var boxTimetableImage: FrameLayout

    private var selectedTimetableUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedTimetableUri = uri
                showSelectedImage(uri)
                uploadTimetableImage(uri)
            } else {
                Toast.makeText(this, "이미지 선택이 취소되었습니다", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        btnEditTimetable = findViewById(R.id.btnEditTimetable)
        boxTimetableImage = findViewById(R.id.boxTimetableImage)

        btnEditTimetable.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun showSelectedImage(uri: Uri) {
        boxTimetableImage.removeAllViews()

        val imageView = ImageView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageURI(uri)
        }

        boxTimetableImage.addView(imageView)
    }

    private fun uploadTimetableImage(uri: Uri) {
        val guestUuid = GuestManager.getGuestUuid(this)

        if (guestUuid.isNullOrBlank()) {
            Toast.makeText(this, "guestUuid가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val file = uriToFile(uri)

                val requestBody = file.readBytes()
                    .toRequestBody("image/*".toMediaTypeOrNull())

                val multipartFile = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    requestBody
                )

                Log.d("TIMETABLE_API", "guestUuid=$guestUuid")
                Log.d("TIMETABLE_API", "fileName=${file.name}")

                val response = RetrofitClient.memberApi.parseTimetableImage(
                    guestUuid = guestUuid,
                    file = multipartFile
                )

                Log.d("TIMETABLE_API", "code=${response.code()}")
                Log.d("TIMETABLE_API", "body=${response.body()}")
                Log.d("TIMETABLE_API", "errorBody=${response.errorBody()?.string()}")

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success && body.result != null) {
                        val timetableData = body.result.timetableData

                        getSharedPreferences("app_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("timetableData", timetableData)
                            .apply()

                        Toast.makeText(
                            this@MypageActivity,
                            "시간표 업로드 및 파싱 성공",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@MypageActivity,
                            body?.message ?: "시간표 파싱 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MypageActivity,
                        "실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("TIMETABLE_API", "업로드 예외", e)
                Toast.makeText(
                    this@MypageActivity,
                    "시간표 업로드 중 오류가 발생했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("파일을 열 수 없습니다.")

        val file = File(cacheDir, "timetable_upload_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        return file
    }
}