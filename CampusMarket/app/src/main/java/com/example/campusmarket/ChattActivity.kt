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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusmarket.data.model.ChatReceiveDto
import com.example.campusmarket.data.model.ChatSendRequest
import com.example.campusmarket.data.model.ProposalRequest
import com.example.campusmarket.data.model.ProposalRespondRequest
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ChattActivity : AppCompatActivity() {

    private lateinit var recyclerChatMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnSuggestDelivery: TextView
    private lateinit var btnSuggestDirect: TextView
    private lateinit var btnCheck: ImageButton
    private lateinit var btnDown: ImageButton
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatMessageAdapter
    private lateinit var btnReport: Button

    private lateinit var btnFabMain: ImageButton
    private lateinit var layoutQuickActions: LinearLayout
    private lateinit var burgerbar: ImageView
    private lateinit var chatSettingOverlay: View
    private lateinit var chatSettingPanel: View
    private lateinit var btnCloseSetting: ImageView
    private lateinit var settingDim: View
    private var isFabOpen = false

    private var chatRoomId: Long = -1L
    private var productId: Long = -1L
    private var myMemberId: Long? = null
    private var guestUuid: String? = null
    private var stompManager: StompManager? = null
    private val gson = Gson()

    // 이미지 선택 런처
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) uploadAndSendImage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chatt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        chatRoomId = intent.getLongExtra("chatRoomId", -1L)
        productId = intent.getLongExtra("productId", -1L)
        guestUuid = GuestManager.getGuestUuid(this)
        myMemberId = GuestManager.getMemberId(this)

        val title = findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "채팅"

        val backBtn = findViewById<ImageButton>(R.id.backbutton)
        backBtn.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
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

        initViews()
        initRecyclerView()
        setupListeners()

        btnFabMain.setOnClickListener { toggleFab() }

        if (chatRoomId != -1L) {
            loadPreviousMessages()
            loadProductInfoForCard()
        }
    }

    // ─── 상품 카드 로드 ─────────────────────────────────────────────────────

    private fun loadProductInfoForCard() {
        if (productId == -1L) return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProductDetail(productId)
                val result = response.result
                val tvCardTitle = findViewById<TextView>(R.id.tvCardTitle)
                val tvCardPrice = findViewById<TextView>(R.id.tvPrice)
                tvCardTitle.text = result.name
                tvCardPrice.text = if (result.isFree) "무료 나눔" else formatPrice(result.price)

                val ivThumb = findViewById<ImageView>(R.id.ivProductThumb)
                val thumbUrl = result.images?.sortedBy { it.displayOrder }
                    ?.firstOrNull()?.originalImageUrl
                    ?: result.displayAssetImageUrl
                if (!thumbUrl.isNullOrBlank()) {
                    Glide.with(this@ChattActivity).load(thumbUrl).into(ivThumb)
                }

                val btnGoProduct = findViewById<Button>(R.id.btnGoProduct)
                btnGoProduct.setOnClickListener {
                    val intent = Intent(this@ChattActivity, ProductDetailActivity::class.java)
                    intent.putExtra("productId", productId)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ─── 이전 메시지 로드 ───────────────────────────────────────────────────

    private fun loadPreviousMessages() {
        val uuid = guestUuid ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getChatMessages(uuid, chatRoomId)
                if (response.isSuccessful) {
                    val messages = response.body()?.result?.messages ?: emptyList()
                    messages.forEach { dto ->
                        val msg = dtoChatMessage(dto)
                        messageList.add(msg)
                    }
                    chatAdapter.notifyDataSetChanged()
                    if (messageList.isNotEmpty()) {
                        recyclerChatMessages.scrollToPosition(messageList.size - 1)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            connectStomp()
        }
    }

    // ─── ChatReceiveDto → ChatMessage 변환 ──────────────────────────────────

    private fun dtoChatMessage(dto: ChatReceiveDto): ChatMessage {
        val isMine = dto.senderId != null && dto.senderId == myMemberId
        val senderName = if (isMine) "" else (dto.senderNickname ?: "알 수 없음")
        val content = dto.content ?: ""
        val time = formatTime(dto.createdAt)
        val type = dto.messageType ?: "TEXT"

        // 제안 메시지인 경우 metadata에서 proposalId, proposalType 파싱
        var proposalId: Long? = null
        var proposalType: String? = null

        if (type == "PROPOSAL") {
            // metadata 또는 content에서 proposalId, proposalType 파싱 시도
            listOf(dto.metadata, dto.content).filterNotNull().forEach { raw ->
                if (proposalId != null && proposalType != null) return@forEach
                try {
                    val meta = gson.fromJson(raw, JsonObject::class.java)
                    if (proposalId == null) proposalId = meta["proposalId"]?.asLong
                    if (proposalType == null) proposalType = meta["proposalType"]?.asString
                } catch (_: Exception) {}
            }
        }

        // TIMETABLE_SHARE: content가 이미지 URL
        val imageUrl = when (type) {
            "TIMETABLE_SHARE", "IMAGE" -> content
            else -> null
        }

        // SYSTEM 메시지: freeSlots metadata 파싱하여 내용 보강
        val displayContent = if (type == "SYSTEM" && !dto.metadata.isNullOrBlank()) {
            buildSystemMessageContent(content, dto.metadata)
        } else {
            content
        }

        return ChatMessage(
            senderName = senderName,
            message = displayContent,
            time = time,
            isMine = isMine,
            messageType = type,
            imageUrl = imageUrl,
            proposalId = proposalId,
            proposalType = proposalType,
            metadata = dto.metadata
        )
    }

    // ─── WebSocket 연결 ─────────────────────────────────────────────────────

    private fun connectStomp() {
        stompManager = StompManager("ws://3.36.120.78:8080/api/ws")

        stompManager?.onConnected = {
            stompManager?.subscribe("/sub/chat/$chatRoomId")
        }

        stompManager?.onMessage = { json ->
            try {
                val dto = gson.fromJson(json, ChatReceiveDto::class.java)
                val msg = dtoChatMessage(dto)
                runOnUiThread {
                    messageList.add(msg)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerChatMessages.scrollToPosition(messageList.size - 1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        stompManager?.onError = { error ->
            runOnUiThread {
                Toast.makeText(this, "연결 오류: $error", Toast.LENGTH_SHORT).show()
            }
        }

        stompManager?.connect()
    }

    // ─── 거래 제안 ──────────────────────────────────────────────────────────

    private fun sendProposal(proposalType: String) {
        val uuid = guestUuid ?: return
        if (chatRoomId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createProposal(
                    guestUuid = uuid,
                    chatRoomId = chatRoomId,
                    request = ProposalRequest(proposalType = proposalType)
                )
                if (response.isSuccessful) {
                    val label = if (proposalType == "LOCKER") "사물함(비대면)" else "대면"
                    Toast.makeText(this@ChattActivity, "$label 거래를 제안했습니다.", Toast.LENGTH_SHORT).show()
                    layoutQuickActions.visibility = View.GONE
                    isFabOpen = false
                } else {
                    Toast.makeText(this@ChattActivity, "거래 제안 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChattActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 제안 수락/거절 ─────────────────────────────────────────────────────

    private fun respondToProposal(proposalId: Long, accept: Boolean) {
        val uuid = guestUuid ?: return
        if (chatRoomId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.respondToProposal(
                    guestUuid = uuid,
                    chatRoomId = chatRoomId,
                    proposalId = proposalId,
                    request = ProposalRespondRequest(accept = accept)
                )
                if (response.isSuccessful) {
                    val msg = if (accept) "거래를 수락했습니다." else "거래를 거절했습니다."
                    Toast.makeText(this@ChattActivity, msg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ChattActivity, "요청 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChattActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── 채팅방 나가기 ──────────────────────────────────────────────────────

    private fun leaveChatRoom() {
        val uuid = guestUuid ?: return
        if (chatRoomId == -1L) return

        AlertDialog.Builder(this)
            .setTitle("채팅방 나가기")
            .setMessage("채팅방에서 나가시겠습니까?")
            .setPositiveButton("나가기") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.apiService.leaveChatRoom(uuid, chatRoomId)
                        if (response.isSuccessful) {
                            stompManager?.disconnect()
                            stompManager = null
                            startActivity(Intent(this@ChattActivity, ChatListActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@ChattActivity, "나가기 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ChattActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // ─── 이미지 업로드 및 전송 ──────────────────────────────────────────────

    private fun uploadAndSendImage(uri: Uri) {
        val uuid = guestUuid ?: return
        if (chatRoomId == -1L) return

        lifecycleScope.launch {
            try {
                val file = uriToFile(uri) ?: run {
                    Toast.makeText(this@ChattActivity, "이미지 변환 실패", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
                val requestBody = file.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

                val response = RetrofitClient.apiService.uploadChatImage(uuid, chatRoomId, part)
                if (!response.isSuccessful) {
                    Toast.makeText(this@ChattActivity, "이미지 업로드 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
                // 업로드 성공 시 서버가 WebSocket으로 자동 브로드캐스트하므로 별도 전송 불필요
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@ChattActivity, "이미지 업로드 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── LOCKER 수락 시 사물함 정보 전송 ──────────────────────────────────────

    private fun sendLockerInfo() {
        val uuid = guestUuid ?: return
        if (chatRoomId == -1L) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.memberApi.getMyProfile(uuid)
                val lockerName = response.body()?.result?.lockerName
                if (!lockerName.isNullOrBlank()) {
                    val request = ChatSendRequest(
                        guestUuid = uuid,
                        messageType = "TEXT",
                        content = "📦 사물함 위치: $lockerName"
                    )
                    stompManager?.send("/pub/chat/$chatRoomId", gson.toJson(request))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ─── 판매 완료 처리 ─────────────────────────────────────────────────────

    private fun showSellCompleteDialog() {
        if (productId == -1L) {
            Toast.makeText(this, "상품 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val uuid = guestUuid ?: return

        android.app.Dialog(this).apply {
            setContentView(R.layout.dialog_sell_complete)
            window?.setBackgroundDrawableResource(android.R.color.transparent)

            val btnConfirm = findViewById<Button>(R.id.btnConfirm)
            val btnCancel = findViewById<Button>(R.id.btnCancel)

            btnConfirm.setOnClickListener {
                dismiss()
                lifecycleScope.launch {
                    try {
                        val response = RetrofitClient.apiService.markProductSold(uuid, productId)
                        if (response.isSuccessful) {
                            Toast.makeText(this@ChattActivity, "판매 완료 처리되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@ChattActivity, "처리 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@ChattActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            btnCancel.setOnClickListener { dismiss() }
            show()
        }
    }

    // ─── UI 초기화 ──────────────────────────────────────────────────────────

    private fun initViews() {
        recyclerChatMessages = findViewById(R.id.recyclerChatMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnSuggestDelivery = findViewById(R.id.btnSuggestDelivery)
        btnSuggestDirect = findViewById(R.id.btnSuggestDirect)
        btnFabMain = findViewById(R.id.btnFabMain)
        layoutQuickActions = findViewById(R.id.layoutQuickActions)
        btnCheck = findViewById(R.id.btnCheck)
        btnDown = findViewById(R.id.btnDown)
        burgerbar = findViewById(R.id.burgerbar)
        chatSettingOverlay = findViewById(R.id.chatSettingOverlay)
        chatSettingPanel = findViewById(R.id.chatSettingPanel)
        btnCloseSetting = findViewById(R.id.btnCloseSetting)
        settingDim = findViewById(R.id.settingDim)
        btnReport = findViewById(R.id.btnReport)
    }

    private fun initRecyclerView() {
        chatAdapter = ChatMessageAdapter(
            items = messageList,
            onProposalAccept = { proposalId, proposalType ->
                respondToProposal(proposalId, accept = true)
                if (proposalType == "LOCKER") {
                    sendLockerInfo()
                }
            },
            onProposalReject = { proposalId ->
                respondToProposal(proposalId, accept = false)
            }
        )
        recyclerChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false
        }
        recyclerChatMessages.adapter = chatAdapter
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMyMessage(text)
                etMessage.text.clear()
            }
        }

        // 이미지 첨부 버튼
        val btnDanger = findViewById<ImageButton>(R.id.btndanger)
        btnDanger.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        // 사물함(비대면) 거래 제안
        btnSuggestDelivery.setOnClickListener {
            sendProposal("LOCKER")
        }

        // 대면 거래 제안
        btnSuggestDirect.setOnClickListener {
            sendProposal("FACE_TO_FACE")
        }

        burgerbar.setOnClickListener { openSettingPanel() }
        btnCloseSetting.setOnClickListener { closeSettingPanel() }
        settingDim.setOnClickListener { closeSettingPanel() }
        btnCheck.setOnClickListener { showSellCompleteDialog() }
        btnReport.setOnClickListener { showReportDialog() }

        // 채팅방 나가기
        val btnLeaveChat = findViewById<Button>(R.id.btnLeaveChat)
        btnLeaveChat.setOnClickListener {
            closeSettingPanel()
            leaveChatRoom()
        }

        // 맨 아래로 스크롤
        btnDown.setOnClickListener {
            if (messageList.isNotEmpty()) {
                recyclerChatMessages.scrollToPosition(messageList.size - 1)
            }
        }
    }

    private fun toggleFab() {
        if (isFabOpen) {
            layoutQuickActions.visibility = View.GONE
        } else {
            layoutQuickActions.visibility = View.VISIBLE
        }
        isFabOpen = !isFabOpen
    }

    private fun showReportDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_report)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnBack = dialog.findViewById<Button>(R.id.btnBack)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmitReport)

        val option1 = dialog.findViewById<TextView>(R.id.option1)
        val option2 = dialog.findViewById<TextView>(R.id.option2)
        val option3 = dialog.findViewById<TextView>(R.id.option3)
        val option4 = dialog.findViewById<TextView>(R.id.option4)

        var selected = ""

        option1.setOnClickListener { selected = "선입금 요구" }
        option2.setOnClickListener { selected = "외부 메신저 유도" }
        option3.setOnClickListener { selected = "욕설 / 비하" }
        option4.setOnClickListener { selected = "기타" }

        btnSubmit.setOnClickListener {
            if (selected.isNotEmpty()) dialog.dismiss()
        }
        btnBack.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun openSettingPanel() {
        chatSettingOverlay.visibility = View.VISIBLE
        chatSettingPanel.post {
            chatSettingPanel.translationX = chatSettingPanel.width.toFloat()
            chatSettingPanel.animate().translationX(0f).setDuration(250).start()
        }
    }

    private fun closeSettingPanel() {
        chatSettingPanel.animate()
            .translationX(chatSettingPanel.width.toFloat())
            .setDuration(250)
            .withEndAction { chatSettingOverlay.visibility = View.GONE }
            .start()
    }

    // ─── 메시지 전송 ────────────────────────────────────────────────────────

    private fun sendMyMessage(text: String) {
        val uuid = guestUuid
        if (uuid.isNullOrBlank() || chatRoomId == -1L) return

        val request = ChatSendRequest(
            guestUuid = uuid,
            messageType = "TEXT",
            content = text
        )
        stompManager?.send("/pub/chat/$chatRoomId", gson.toJson(request))
    }

    // ─── 유틸 ───────────────────────────────────────────────────────────────

    private fun buildSystemMessageContent(content: String, metadata: String): String {
        return try {
            val meta = gson.fromJson(metadata, JsonObject::class.java)
            val freeSlots = meta["freeSlots"]?.asJsonArray ?: return content

            val sb = StringBuilder()
            sb.append(if (content.isNotBlank()) content else "서로 비어있는 시간대")
            sb.append("\n")
            freeSlots.forEach { element ->
                val slot = element.asJsonObject
                val day = slot["day"]?.asString ?: return@forEach
                val start = slot["start_time"]?.asString ?: return@forEach
                val end = slot["end_time"]?.asString ?: return@forEach
                sb.append("$day  $start ~ $end\n")
            }
            sb.trimEnd().toString()
        } catch (e: Exception) {
            content
        }
    }

    private fun formatTime(createdAt: String?): String {
        if (createdAt == null) return getCurrentTimeText()
        return try {
            val timePart = createdAt.substringAfter("T").take(5)
            if (timePart.length == 5) timePart else getCurrentTimeText()
        } catch (e: Exception) {
            getCurrentTimeText()
        }
    }

    private fun getCurrentTimeText(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    private fun formatPrice(price: Int): String {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale.KOREA).format(price) + "원"
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val fileName = getFileName(uri) ?: "chat_image_${System.currentTimeMillis()}.jpg"
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

    override fun onDestroy() {
        super.onDestroy()
        stompManager?.disconnect()
        stompManager = null
    }
}
