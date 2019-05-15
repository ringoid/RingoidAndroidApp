package com.ringoid.origin.feed.adapter.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.collection.EqualRange
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*

abstract class BaseFeedAdapter(diffCb: BaseDiffCallback<FeedItemVO>, headerRows: Int = 0)
    : BaseListAdapter<FeedItemVO, OriginFeedViewHolder>(diffCb, headerRows = headerRows) {

    var onBeforeLikeListener: (() -> Boolean)? = null
    var onImageTouchListener: ((x: Float, y: Float) -> Unit)? = null
    var onFeedItemRemoveListener: ((position: Int) -> Unit)? = null
    var settingsClickListener: ((model: FeedItemVO, position: Int, positionOfImage: Int) -> Unit)? = null
    internal var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    protected var imagesViewPool = RecyclerView.RecycledViewPool()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OriginFeedViewHolder {
        val viewHolder = super.onCreateViewHolder(parent, viewType)
        return viewHolder  // perform additional initialization only for VIEW_TYPE_NORMAL view holders
            .takeIf { viewType == VIEW_TYPE_NORMAL }
            ?.also { vh ->
                vh.onBeforeLikeListener = onBeforeLikeListener
                vh.onImageTouchListener = onImageTouchListener
                vh.snapPositionListener = { positionOfImage ->
                    vh.adapterPosition
                        .takeIf { it != RecyclerView.NO_POSITION }
                        ?.let { getModel(it).positionOfImage = positionOfImage }
                }
                vh.trackingBus = this@BaseFeedAdapter.trackingBus
                val wrapSettingsClickListener: ((model: FeedItemVO, position: Int) -> Unit)? =
                    { model: FeedItemVO, position: Int -> settingsClickListener?.invoke(model, position, vh.getCurrentImagePosition()) }
                vh.itemView.ibtn_settings.clicks().compose(clickDebounce())
                    .subscribe { wrapOnItemClickListener(vh, wrapSettingsClickListener).onClick(vh.itemView.ibtn_settings) }
                vh.setOnClickListener(null)  // clicks on the whole feed's item is no-op
            } ?: viewHolder  // don't apply additional initializations on non-VIEW_TYPE_NORMAL view holders
    }

    override fun getOnRemovedCb(): ((position: Int, count: Int) -> Unit)? =
        { position, _ -> onFeedItemRemoveListener?.invoke(position) }

    // ------------------------------------------
    override fun getFooterLayoutResId(): Int = R.layout.rv_item_lmm_footer

    /**
     * Wraps outer click on image listener, but passes feed item position in adapter instead of image's
     * position in the inner adapter. This is useful when callers need just a callback on image click
     * and position of enclosing feed item that contains that clicked image.
     */
    protected fun wrapOnImageClickListenerByFeedItem(vh: OriginFeedViewHolder, l: ((model: ProfileImageVO, position: Int) -> Unit)?)
            : ((model: ProfileImageVO, position: Int) -> Unit)? =
        { model: ProfileImageVO, _ -> vh.adapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { l?.invoke(model, it) } }
}
