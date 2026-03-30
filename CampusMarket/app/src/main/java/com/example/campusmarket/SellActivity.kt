package com.example.campusmarket

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusmarket.data.model.BackgroundRemovalRequest
import com.example.campusmarket.RetrofitClient
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class SellActivity : AppCompatActivity() {

    private lateinit var checkRemoveBg: CheckBox

    private lateinit var layoutPhotoUpload1: LinearLayout
    private lateinit var ivPhotoPreview1: ImageView

    private var selectedSlotIndex: Int = -1

    private val selectedImageUris = MutableList<Uri?>(5) { null }
    private val tempImageIds = MutableList<Long?>(5) { null }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null && selectedSlotIndex in 0..4) {
                selectedImageUris[selectedSlotIndex] = uri

                when (selectedSlotIndex) {
                    0 -> ivPhotoPreview1.setImageURI(uri)
                }

                uploadSingleTempImage(selectedSlotIndex, uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        checkRemoveBg = findViewById(R.id.checkRemoveBg)

        layoutPhotoUpload1 = findViewById(R.id.layoutPhotoUpload1)
        ivPhotoPreview1 = findViewById(R.id.ivPhotoPreview1)

        layoutPhotoUpload1.setOnClickListener {
            selectedSlotIndex = 0
            pickImageLauncher.launch("image/*")
        }

        checkRemoveBg.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                removeBackgroundForUploadedImages()
            }
        }
    }

    private fun uploadSingleTempImage(index: Int, uri: Uri) {
        val imagePart = uriToMultipart(uri)

        if (imagePart == null) {
            Toast.makeText(this, "이미지 변환 실패", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productImageApi.uploadTempImage(
                    guestUuid = "550e8400-e29b-41d4-a716-446655440000", // 비회원 예시
                    memberId = null,
                    files = imagePart
                )

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success && body.result != null) {
                        val result = body.result
                        tempImageIds[index] = result.tempImageId

                        Log.d("BG_REMOVE", "temp upload success: slot=$index, tempImageId=${result.tempImageId}")

                        // 이미 체크된 상태라면 업로드 직후 바로 배경제거
                        if (checkRemoveBg.isChecked) {
                            removeBackgroundForUploadedImages()
                        }
                    } else {
                        Toast.makeText(this@SellActivity, "임시 업로드 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("BG_REMOVE", "temp upload code=${response.code()}")
                    Log.e("BG_REMOVE", "temp upload error=${response.errorBody()?.string()}")
                    Toast.makeText(this@SellActivity, "임시 업로드 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("BG_REMOVE", "temp upload exception=${e.message}", e)
                Toast.makeText(this@SellActivity, "업로드 중 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeBackgroundForUploadedImages() {
        val validIds = tempImageIds.filterNotNull()

        if (validIds.isEmpty()) {
            Toast.makeText(this, "먼저 사진을 업로드해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.productImageApi.removeBackground(
                    guestUuid = "550e8400-e29b-41d4-a716-446655440000",
                    memberId = null,
                    request = BackgroundRemovalRequest(tempImageIds = validIds)
                )

                if (response.isSuccessful) {
                    val body = response.body()

                    if (body != null && body.success && body.result != null) {
                        val items = body.result.items

                        for (item in items) {
                            val slotIndex = tempImageIds.indexOf(item.tempImageId)
                            val bgRemovedUrl = item.backgroundRemovedImageUrl

                            Log.d("BG_REMOVE", "remove success: tempImageId=${item.tempImageId}, url=$bgRemovedUrl")

                            if (slotIndex != -1 && !bgRemovedUrl.isNullOrBlank()) {
                                when (slotIndex) {
                                    0 -> loadImageFromUrl(ivPhotoPreview1, bgRemovedUrl)
                                }
                            }
                        }

                        Toast.makeText(this@SellActivity, "배경 제거 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SellActivity, "배경 제거 실패", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("BG_REMOVE", "remove code=${response.code()}")
                    Log.e("BG_REMOVE", "remove error=${response.errorBody()?.string()}")
                    Toast.makeText(this@SellActivity, "배경 제거 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("BG_REMOVE", "remove exception=${e.message}", e)
                Toast.makeText(this@SellActivity, "배경 제거 중 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        return try {
            val resolver = contentResolver
            val inputStream = resolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val requestBody = bytes.toRequestBody()

            MultipartBody.Part.createFormData(
                "files",
                "upload_image.jpg",
                requestBody
            )
        } catch (e: Exception) {
            Log.e("BG_REMOVE", "uriToMultipart error=${e.message}", e)
            null
        }
    }

    private fun loadImageFromUrl(imageView: ImageView, imageUrl: String) {
        Thread {
            try {
                val input = java.net.URL(imageUrl).openStream()
                val bitmap = android.graphics.BitmapFactory.decodeStream(input)

                runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            } catch (e: Exception) {
                Log.e("BG_REMOVE", "load url image error=${e.message}", e)
            }
        }.start()
    }
}