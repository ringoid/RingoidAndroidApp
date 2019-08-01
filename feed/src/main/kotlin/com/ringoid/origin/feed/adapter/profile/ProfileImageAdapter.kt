package com.ringoid.origin.feed.adapter.profile

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.EmptyProfileImageVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.image.ImageLoader
import com.ringoid.utility.image.ImageRequest

class ProfileImageAdapter(private val imageLoader: ImageRequest)
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
        holder.setOnDoubleClickListener(getOnItemDoubleClickListener(holder))
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: BaseProfileImageViewHolder, position: Int, payloads: List<Any>) {
        holder.setOnClickListener(getOnItemClickListener(holder))
        holder.setOnDoubleClickListener(getOnItemDoubleClickListener(holder))
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onViewRecycled(holder: BaseProfileImageViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanUp()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        dispose()
    }

    override fun dispose() {
        super.dispose()
        onBeforeLikeListener = null
        onImageTouchListener = null
        tabsObserver = null
    }

    override fun getExposedCb(): (() -> Unit)? = { tabsObserver?.onChanged() }

    // --------------------------------------------------------------------------------------------
    override fun getStubItem(): ProfileImageVO = EmptyProfileImageVO

    // ------------------------------------------
    override fun getPreloadItems(position: Int): List<ProfileImageVO> =
        getModels().subList(position, position + 1)

    override fun getPreloadRequestBuilder(item: ProfileImageVO): RequestBuilder<*>? =
        ImageLoader.simpleLoadRequest(imageLoader = imageLoader, uri = item.image.thumbnailUri)

    override fun getOnItemDoubleClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener =
        if (!isLikeEnabled) {
            super.getOnItemDoubleClickListener(vh)
        } else {
            wrapOnItemClickListener(vh, getLikeClickListener(vh))
        }

    private fun getLikeClickListener(vh: BaseProfileImageViewHolder)
        : ((model: ProfileImageVO, position: Int) -> Unit)? =
            { model: ProfileImageVO, position: Int ->
                if (onBeforeLikeListener?.invoke() != false) {
                    itemDoubleClickListener?.invoke(model, position)
                    vh.itemView.tag
                        ?.let { it as MotionEvent }
                        ?.let { onImageTouchListener?.invoke(it.rawX, it.rawY) }
                }
            }
}
