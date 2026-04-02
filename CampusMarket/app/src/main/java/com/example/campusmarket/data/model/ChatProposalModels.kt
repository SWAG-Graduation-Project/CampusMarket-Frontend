package com.example.campusmarket.data.model

data class ProposalRequest(
    val proposalType: String // "LOCKER" or "FACE_TO_FACE"
)

data class ProposalRespondRequest(
    val accept: Boolean
)

data class ProposalResult(
    val proposalId: Long,
    val chatRoomId: Long,
    val proposalType: String,
    val proposalStatus: String, // "PENDING", "ACCEPTED", "REJECTED"
    val proposalMessageId: Long?,
    val responseMessageId: Long?,
    val proposedAt: String?,
    val respondedAt: String?
)

data class ProposalResponse(
    val code: String,
    val message: String,
    val result: ProposalResult?,
    val success: Boolean
)

data class ChatImageUploadResult(
    val imageUrl: String
)

data class ChatImageUploadResponse(
    val code: String,
    val message: String,
    val result: ChatImageUploadResult?,
    val success: Boolean
)
