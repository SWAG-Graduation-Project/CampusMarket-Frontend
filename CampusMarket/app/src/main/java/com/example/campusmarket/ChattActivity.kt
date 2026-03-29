package com.example.campusmarket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chatt)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        val title = findViewById<TextView>(R.id.tvHeaderTitle)
        title.text = "채팅"

        val backBtn = findViewById<ImageButton>(R.id.backbutton)

        backBtn.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }
        val homebutton = findViewById<LinearLayout>(R.id.gohome)

        homebutton.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            startActivity(intent)
        }

        val goMymarket = findViewById<LinearLayout>(R.id.goMymarket)

        goMymarket.setOnClickListener {
            val intent = Intent(this, MyMarketActivity::class.java)
            startActivity(intent)
        }

        val goMypage = findViewById<LinearLayout>(R.id.gomypage)

        goMypage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        val gochat = findViewById<LinearLayout>(R.id.gochat)

        gochat.setOnClickListener {
            val intent = Intent(this, ChatListActivity::class.java)
            startActivity(intent)
        }




        initViews()
        initRecyclerView()
        loadDummyMessages()
        setupListeners()
        btnFabMain.setOnClickListener {
            toggleFab()
        }
        // 나중에 소켓 연동 시 여기서 연결
        // connectSocket()
    }
    private fun toggleFab() {
        if (isFabOpen) {
            layoutQuickActions.visibility = View.GONE

        } else {
            layoutQuickActions.visibility = View.VISIBLE

        }
        isFabOpen = !isFabOpen
    }
    private fun showSellCompleteDialog() {
        val dialog = android.app.Dialog(this)
        dialog.setContentView(R.layout.dialog_sell_complete)

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        btnConfirm.setOnClickListener {
            // TODO: 판매 완료 처리
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
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
        chatAdapter = ChatMessageAdapter(messageList)

        recyclerChatMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = false
        }
        recyclerChatMessages.adapter = chatAdapter
    }

    private fun loadDummyMessages() {
        messageList.add(
            ChatMessage(
                senderName = "파닥부단",
                message = "안녕하세요. 구매 가능할까요?",
                time = "23:21",
                isMine = false
            )
        )

        messageList.add(
            ChatMessage(
                senderName = "",
                message = "안녕하세요. 가능합니다.",
                time = "23:23",
                isMine = true
            )
        )

        chatAdapter.notifyDataSetChanged()
        recyclerChatMessages.scrollToPosition(messageList.size - 1)
    }

    private fun setupListeners() {
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMyMessage(text)
                etMessage.text.clear()
            }
        }

        btnSuggestDelivery.setOnClickListener {
            sendMyMessage("사물함 거래를 제안할게요.")
        }

        btnSuggestDirect.setOnClickListener {
            sendMyMessage("대면 거래를 제안할게요.")
        }

        burgerbar.setOnClickListener {
            openSettingPanel()
        }

        btnCloseSetting.setOnClickListener {
            closeSettingPanel()
        }

        settingDim.setOnClickListener {
            closeSettingPanel()
        }
        btnCheck.setOnClickListener {
            showSellCompleteDialog()
        }
        btnReport.setOnClickListener {
            showReportDialog()
        }

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
            if (selected.isNotEmpty()) {
                // TODO: 신고 API 호출
                dialog.dismiss()
            }
        }

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
    private fun openSettingPanel() {
        chatSettingOverlay.visibility = View.VISIBLE

        chatSettingPanel.post {
            chatSettingPanel.translationX = chatSettingPanel.width.toFloat()
            chatSettingPanel.animate()
                .translationX(0f)
                .setDuration(250)
                .start()
        }
    }

    private fun closeSettingPanel() {
        chatSettingPanel.animate()
            .translationX(chatSettingPanel.width.toFloat())
            .setDuration(250)
            .withEndAction {
                chatSettingOverlay.visibility = View.GONE
            }
            .start()
    }
    private fun sendMyMessage(text: String) {
        val newMessage = ChatMessage(
            senderName = "",
            message = text,
            time = getCurrentTimeText(),
            isMine = true
        )

        messageList.add(newMessage)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        recyclerChatMessages.scrollToPosition(messageList.size - 1)

        // 나중에 소켓 API 연동 시 사용
        // sendMessageToSocket(text)
    }

    private fun receiveMessageFromSocket(sender: String, message: String, time: String) {
        val newMessage = ChatMessage(
            senderName = sender,
            message = message,
            time = time,
            isMine = false
        )

        runOnUiThread {
            messageList.add(newMessage)
            chatAdapter.notifyItemInserted(messageList.size - 1)
            recyclerChatMessages.scrollToPosition(messageList.size - 1)
        }
    }

    private fun getCurrentTimeText(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }
    private lateinit var btnFabMain: ImageButton
    private lateinit var layoutQuickActions: LinearLayout
    private lateinit var burgerbar: ImageView
    private lateinit var chatSettingOverlay: View
    private lateinit var chatSettingPanel: View
    private lateinit var btnCloseSetting: ImageView
    private lateinit var settingDim: View
    private var isFabOpen = false
    // =========================
    // 소켓 연동용 자리
    // =========================
    /*
    private fun connectSocket() {
        // 예:
        // 1. 소켓 연결
        // 2. 채팅방 입장
        // 3. 수신 리스너 등록
        // 4. 메시지 오면 receiveMessageFromSocket(...) 호출
    }

    private fun sendMessageToSocket(message: String) {
        // 예:
        // socket.emit("send_message", ...)
    }

    override fun onDestroy() {
        super.onDestroy()
        // socket disconnect / listener 해제
    }
    */
}