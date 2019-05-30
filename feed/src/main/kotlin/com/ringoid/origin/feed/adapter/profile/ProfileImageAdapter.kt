package com.ringoid.origin.feed.adapter.profile

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.EmptyProfileImageVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.image.ImageLoader

class ProfileImageAdapter(private val context: Context)
    : BaseListAdapter<ProfileImageVO, BaseProfileImageViewHolder>(ProfileImageDiffCallback(), withListeners = false),
    ListPreloader.PreloadModelProvider<ProfileImageVO> {

    var onBeforeLikeListener: (() -> Boolean)? = null
    var onImageTouchListener: ((x: Float, y: Float) -> Unit)? = null
    var tabsObserver: RecyclerView.AdapterDataObserver? = null
    var isLikeEnabled = true

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): BaseProfileImageViewHolder = ProfileImageViewHolder(view)

    override fun instantiateHeaderViewHolder(view: View) = HeaderProfileImageViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterProfileImageViewHolder(view)

    override fun onBindViewHolder(holder: BaseProfileImageViewHolder, position: Int) {
        holder.setOnClickListener(getOnItemClickListener(holder))
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: BaseProfileImageViewHolder, position: Int, payloads: List<Any>) {
        holder.setOnClickListener(getOnItemClickListener(holder))
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getExposedCb(): (() -> Unit)? = { tabsObserver?.onChanged() }

    // --------------------------------------------------------------------------------------------
    override fun getStubItem(): ProfileImageVO = EmptyProfileImageVO

    // ------------------------------------------
    override fun getPreloadItems(position: Int): MutableList<ProfileImageVO> =
        getModels().subList(position, position + 1).toMutableList()

    override fun getPreloadRequestBuilder(item: ProfileImageVO): RequestBuilder<*>? =
        ImageLoader.loadRequest(uri = item.image.uri,
            context = context, options = RequestOptions().centerCrop().override(AppRes.SCREEN_WIDTH, AppRes.FEED_IMAGE_HEIGHT))

    override fun getOnItemClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener {
        val clickListener = if (!isLikeEnabled) {
            super.getOnItemClickListener(vh)
        } else {
            wrapOnItemClickListener(vh, getLikeClickListener(vh, alwaysLiked = true))
        }
        // detect touch on image item and call visual effect at touch point
        vh.itemView.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.tag =  event
            }
            false
        }
        return clickListener
    }

    private fun getLikeClickListener(vh: BaseProfileImageViewHolder, alwaysLiked: Boolean = false)
        : ((model: ProfileImageVO, position: Int) -> Unit)? =
            { model: ProfileImageVO, position: Int ->
                if (onBeforeLikeListener?.invoke() != false) {
                    val isLiked = if (alwaysLiked) true else !model.isLiked
                    model.isLiked = isLiked
                    if (isLiked) {  // animate on Like, don't animate on Unlike
                        notifyItemChanged(vh.adapterPosition, ProfileImageViewHolderAnimateLike)
                    }
                    itemClickListener?.invoke(model, position)
                    vh.itemView.tag
                        ?.let { it as MotionEvent }
                        ?.let { onImageTouchListener?.invoke(it.x, it.y) }
                }
            }
}
