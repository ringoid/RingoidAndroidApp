package com.ringoid.origin.feed.adapter.lmm.like

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.adapter.FeedAdapter
import com.ringoid.origin.feed.adapter.ProfileViewHolder
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_feed_profile.view.*

open class LikeFeedAdapter(imagesViewPool: RecyclerView.RecycledViewPool? = null) : FeedAdapter(imagesViewPool) {

    var messageClickListener: ((model: Profile, position: Int) -> Unit)? = null

    override fun instantiateViewHolder(view: View): ProfileViewHolder =
        super.instantiateViewHolder(view).also { vh ->
            vh.itemView.ibtn_message.apply {
                changeVisibility(isVisible = true)
                clicks().compose(clickDebounce())
                    .subscribe { wrapOnItemClickListener(vh, messageClickListener).onClick(vh.itemView.ibtn_message) }
            }
        }
}
