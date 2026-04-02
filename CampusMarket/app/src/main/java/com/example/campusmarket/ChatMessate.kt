package com.example.campusmarket

data class ChatMessage(
    val senderName: String,
    val message: String,
    val time: String,
    val isMine: Boolean,
    val messageType: String = "TEXT",   // TEXT, IMAGE, SYSTEM, TIMETABLE_SHARE, PROPOSAL
    val imageUrl: String? = null,
    val proposalId: Long? = null,
    val proposalType: String? = null,   // LOCKER or FACE_TO_FACE
    val metadata: String? = null
)
