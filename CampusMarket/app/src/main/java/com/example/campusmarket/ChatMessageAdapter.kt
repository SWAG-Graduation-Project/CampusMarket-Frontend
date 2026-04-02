package com.example.campusmarket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ChatMessageAdapter(
    private val items: MutableList<ChatMessage>,
    private val onProposalAccept: ((proposalId: Long, proposalType: String) -> Unit)? = null,
    private val onProposalReject: ((proposalId: Long) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_OTHER = 0
        private const val TYPE_ME = 1
        private const val TYPE_SYSTEM = 2
        private const val TYPE_IMAGE_ME = 3
        private const val TYPE_IMAGE_OTHER = 4
        private const val TYPE_PROPOSAL = 5      // 상대방이 보낸 제안 (수락/거절 버튼 포함)
        private const val TYPE_PROPOSAL_SENT = 6 // 내가 보낸 제안 (단순 텍스트)
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item.messageType) {
            "SYSTEM" -> TYPE_SYSTEM
            "TIMETABLE_SHARE" -> if (item.isMine) TYPE_IMAGE_ME else TYPE_IMAGE_OTHER
            "IMAGE" -> if (item.isMine) TYPE_IMAGE_ME else TYPE_IMAGE_OTHER
            "PROPOSAL" -> {
                if (item.isMine) TYPE_PROPOSAL_SENT else TYPE_PROPOSAL
            }
            else -> if (item.isMine) TYPE_ME else TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ME -> MyMessageViewHolder(inflater.inflate(R.layout.item_chat_me, parent, false))
            TYPE_OTHER -> OtherMessageViewHolder(inflater.inflate(R.layout.item_chat_other, parent, false))
            TYPE_SYSTEM -> SystemMessageViewHolder(inflater.inflate(R.layout.item_chat_system, parent, false))
            TYPE_IMAGE_ME -> MyImageViewHolder(inflater.inflate(R.layout.item_chat_image_me, parent, false))
            TYPE_IMAGE_OTHER -> OtherImageViewHolder(inflater.inflate(R.layout.item_chat_image_other, parent, false))
            TYPE_PROPOSAL -> ProposalViewHolder(inflater.inflate(R.layout.item_chat_offer_notice, parent, false))
            TYPE_PROPOSAL_SENT -> MyMessageViewHolder(inflater.inflate(R.layout.item_chat_me, parent, false))
            else -> MyMessageViewHolder(inflater.inflate(R.layout.item_chat_me, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is MyMessageViewHolder -> holder.bind(item)
            is OtherMessageViewHolder -> holder.bind(item)
            is SystemMessageViewHolder -> holder.bind(item)
            is MyImageViewHolder -> holder.bind(item)
            is OtherImageViewHolder -> holder.bind(item)
            is ProposalViewHolder -> holder.bind(item, onProposalAccept, onProposalReject)
        }
    }

    override fun getItemCount(): Int = items.size

    // ─── ViewHolders ──────────────────────────────────────────────────────────

    class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMyMessage: TextView = itemView.findViewById(R.id.tvMyMessage)
        private val tvMyTime: TextView = itemView.findViewById(R.id.tvMyTime)

        fun bind(item: ChatMessage) {
            tvMyMessage.text = item.message
            tvMyTime.text = item.time
        }
    }

    class OtherMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvOtherMessage: TextView = itemView.findViewById(R.id.tvOtherMessage)
        private val tvOtherTime: TextView = itemView.findViewById(R.id.tvOtherTime)

        fun bind(item: ChatMessage) {
            tvSenderName.text = item.senderName
            tvOtherMessage.text = item.message
            tvOtherTime.text = item.time
        }
    }

    class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSystemMessage: TextView = itemView.findViewById(R.id.tvSystemMessage)

        fun bind(item: ChatMessage) {
            tvSystemMessage.text = item.message
        }
    }

    class MyImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivMyImage: ImageView = itemView.findViewById(R.id.ivMyImage)
        private val tvMyImageTime: TextView = itemView.findViewById(R.id.tvMyImageTime)

        fun bind(item: ChatMessage) {
            tvMyImageTime.text = item.time
            val url = item.imageUrl ?: item.message
            if (url.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivMyImage)
            }
        }
    }

    class OtherImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivOtherImage: ImageView = itemView.findViewById(R.id.ivOtherImage)
        private val tvOtherImageSender: TextView = itemView.findViewById(R.id.tvOtherImageSender)
        private val tvOtherImageTime: TextView = itemView.findViewById(R.id.tvOtherImageTime)

        fun bind(item: ChatMessage) {
            tvOtherImageSender.text = item.senderName
            tvOtherImageTime.text = item.time
            val url = item.imageUrl ?: item.message
            if (url.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivOtherImage)
            }
        }
    }

    class ProposalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOfferNotice: TextView = itemView.findViewById(R.id.tvOfferNotice)
        private val btnAccept: ImageButton = itemView.findViewById(R.id.btnOfferAccept)
        private val btnReject: ImageButton = itemView.findViewById(R.id.btnOfferReject)

        fun bind(
            item: ChatMessage,
            onAccept: ((Long, String) -> Unit)?,
            onReject: ((Long) -> Unit)?
        ) {
            val typeLabel = when (item.proposalType) {
                "LOCKER" -> "사물함(비대면)"
                "FACE_TO_FACE" -> "대면"
                else -> ""
            }
            tvOfferNotice.text = "상대방이 $typeLabel 거래를 제안했어요\n수락하시겠어요?"

            val pid = item.proposalId
            val ptype = item.proposalType ?: ""

            if (pid != null) {
                btnAccept.visibility = View.VISIBLE
                btnReject.visibility = View.VISIBLE
                btnAccept.setOnClickListener { onAccept?.invoke(pid, ptype) }
                btnReject.setOnClickListener { onReject?.invoke(pid) }
            } else {
                btnAccept.visibility = View.GONE
                btnReject.visibility = View.GONE
            }
        }
    }
}
