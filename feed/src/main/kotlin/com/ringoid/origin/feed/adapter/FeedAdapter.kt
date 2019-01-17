package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.feed.Feed
import com.ringoid.domain.model.feed.Profile
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_feed_profile.view.*

open class FeedAdapter(private var imagesViewPool: RecyclerView.RecycledViewPool? = null)
    : BaseListAdapter<Profile, ProfileViewHolder>(ProfileDiffCallback()) {

    var onLikeImageListener: ((model: ProfileImageVO, position: Int) -> Unit)? = null
    var settingsClickListener: ((model: Profile, position: Int, positionOfImage: Int) -> Unit)? = null

    init {
        imagesViewPool = imagesViewPool ?: RecyclerView.RecycledViewPool()
    }

    override fun getLayoutId(): Int = R.layout.rv_item_feed_profile

    override fun instantiateViewHolder(view: View): ProfileViewHolder =
        ProfileViewHolder(view, viewPool = imagesViewPool).apply {
            onLikeImageListener = this@FeedAdapter.onLikeImageListener
            val wrapSettingsClickListener: ((model: Profile, position: Int) -> Unit)? =
                { model: Profile, position: Int ->
                    settingsClickListener?.invoke(model, position, getCurrentImagePosition())
                }
            itemView.ibtn_settings.clicks().compose(clickDebounce())
                .subscribe { wrapOnItemClickListener(this, wrapSettingsClickListener).onClick(itemView.ibtn_settings) }
        }

    fun submit(feed: Feed) {
        submitList(feed.profiles)
    }
}

// ------------------------------------------------------------------------------------------------
class ProfileDiffCallback : BaseDiffCallback<Profile>() {

    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem == newItem  // as 'data class'
}
