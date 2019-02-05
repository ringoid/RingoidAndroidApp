package com.ringoid.origin.messenger.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.longClicks
import com.ringoid.base.adapter.OriginListAdapter
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.EmptyMessage
import com.ringoid.domain.model.messenger.Message
import com.ringoid.origin.messenger.R
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_chat_item.view.*

class ChatAdapter : OriginListAdapter<Message, BaseChatViewHolder>(MessageDiffCallback()) {

    companion object {
        const val VIEW_TYPE_NORMAL_MY = 5
    }

    var onMessageClickListener: ((model: Message, position: Int) -> Unit)? = null
    var onMessageInsertListener: ((position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseChatViewHolder {
        val layoutResId = when (viewType) {
            VIEW_TYPE_FOOTER -> getFooterLayoutResId()
            VIEW_TYPE_NORMAL -> R.layout.rv_item_chat_item
            VIEW_TYPE_NORMAL_MY -> R.layout.rv_item_my_chat_item
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return when (viewType) {
            VIEW_TYPE_FOOTER -> HeaderChatViewHolder(view)
            VIEW_TYPE_NORMAL -> PeerChatViewHolder(view)
            VIEW_TYPE_NORMAL_MY -> MyChatViewHolder(view)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }.also { vh->
            if (viewType != VIEW_TYPE_NORMAL && viewType != VIEW_TYPE_NORMAL_MY) {
                return@also
            }

            vh.setOnClickListener(getOnItemClickListener(vh))
            vh.itemView.tv_chat_message.also { view ->
                view.clicks().compose(clickDebounce()).subscribe { getOnItemClickListener(vh).onClick(view) }
                view.longClicks().compose(clickDebounce())
                    .subscribe { wrapOnItemClickListener(vh, onMessageClickListener).onClick(view) }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        val viewType = getItemViewType(position)
        return when (viewType) {
            VIEW_TYPE_NORMAL, VIEW_TYPE_NORMAL_MY -> getModel(position).getModelId()
            else -> viewType.toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val viewType = super.getItemViewType(position)
        return when (viewType) {
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

    // ------------------------------------------
    override fun getStubItem(): Message = EmptyMessage
    override fun getFooterLayoutResId(): Int = R.layout.rv_item_chat_header
}
