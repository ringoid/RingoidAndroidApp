package com.ringoid.origin.feed.adapter.lmm.messenger

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.domain.model.feed.FeedItem
import com.ringoid.origin.feed.adapter.lmm.BaseLmmViewHolder
import com.ringoid.origin.feed.adapter.lmm.LmmViewHolder

interface IMessengerViewHolder

abstract class OriginMessengerViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : LmmViewHolder(view, viewPool), IMessengerViewHolder

abstract class BaseMessengerViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseLmmViewHolder(view, viewPool), IMessengerViewHolder

class MessengerViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseMessengerViewHolder(view, viewPool)

class HeaderMessengerViewHolder(view: View) : OriginMessengerViewHolder(view), IMessengerViewHolder {

    override fun bind(model: FeedItem) {
        // no-op
    }
}
