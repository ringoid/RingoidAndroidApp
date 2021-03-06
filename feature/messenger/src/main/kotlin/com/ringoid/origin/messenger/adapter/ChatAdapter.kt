package com.ringoid.origin.messenger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.longClicks
import com.ringoid.base.adapter.OriginListAdapter
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.EmptyMessage
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.messenger.R
import com.ringoid.utility.clickDebounce
import timber.log.Timber

class ChatAdapter : OriginListAdapter<Message, BaseChatViewHolder>(MessageDiffCallback()) {

    companion object {
        const val VIEW_TYPE_NORMAL_MY = 5
    }

    internal var onMessageInsertListener: ((position: Int) -> Unit)? = null
    internal var onMessageLongClickListener: ((message: Message) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChatViewHolder {
        val layoutResId = when (viewType) {
            VIEW_TYPE_FOOTER -> getFooterLayoutResId()
            VIEW_TYPE_NORMAL -> R.layout.rv_item_chat_item
            VIEW_TYPE_NORMAL_MY -> R.layout.rv_item_my_chat_item
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        val viewHolder = when (viewType) {
            VIEW_TYPE_FOOTER -> HeaderChatViewHolder(view).also {
                it.setOnClickListener(getOnItemClickListener(it))
                it.setOnDoubleClickListener(getOnItemDoubleClickListener(it))
            }
            VIEW_TYPE_NORMAL -> if (BuildConfig.IS_STAGING) DebugPeerChatViewHolder(view) else PeerChatViewHolder(view)
            VIEW_TYPE_NORMAL_MY -> if (BuildConfig.IS_STAGING) DebugMyChatViewHolder(view) else MyChatViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }

        return viewHolder  // perform additional initialization only for VIEW_TYPE_NORMAL view holders
            .takeIf { viewType == VIEW_TYPE_NORMAL || viewType == VIEW_TYPE_NORMAL_MY }
            ?.also { vh ->
                vh.setOnClickListener(getOnItemClickListener(vh))
                vh.setOnDoubleClickListener(getOnItemDoubleClickListener(vh))
                vh.itemView.longClicks().compose(clickDebounce()).subscribe {
                    vh.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }
                        ?.let { findModel(it) }
                        ?.let { onMessageLongClickListener?.invoke(it) }
                }
            } ?: viewHolder  // don't apply additional initializations on non-VIEW_TYPE_NORMAL view holders
    }

    override fun getItemId(position: Int): Long =
        getItemViewType(position).let { viewType ->
            when (viewType) {
                VIEW_TYPE_NORMAL, VIEW_TYPE_NORMAL_MY -> getModel(position).getModelId()
                else -> viewType.toLong()
            }
        }

    override fun getItemViewType(position: Int): Int =
        super.getItemViewType(position).let { viewType ->
            when (viewType) {
                VIEW_TYPE_NORMAL -> {
                    if (getModel(position).peerId == DomainUtil.CURRENT_USER_ID) VIEW_TYPE_NORMAL_MY
                    else viewType
                }
                else -> viewType
            }
        }

    // ------------------------------------------
    override fun getOnInsertedCb(): ((position: Int, count: Int) -> Unit)? =
        { position: Int, _ -> onMessageInsertListener?.invoke(position) }

    override fun getOnRemovedCb(): ((position: Int, count: Int) -> Unit)? =
        { position: Int, count -> Timber.w("List removed [$count] ${getModel(position).text}") }

    // ------------------------------------------
    override fun getStubItem(): Message = EmptyMessage
    override fun getFooterLayoutResId(): Int = R.layout.rv_item_chat_header

    // --------------------------------------------------------------------------------------------
    internal fun updateReadStatusOnMessages(messages: List<Message>) {
        if (messages.isEmpty()) {
            return
        }

        messages.forEach { message ->
            findModel { it.id == message.id }?.readStatus = message.readStatus
        }
        notifyDataSetChanged()
    }
}
