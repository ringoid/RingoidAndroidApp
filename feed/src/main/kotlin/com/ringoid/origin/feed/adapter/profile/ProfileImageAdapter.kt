package com.ringoid.origin.feed.adapter.profile

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.EmptyProfileImageVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.view.common.visual.LikeVisualEffect
import com.ringoid.origin.view.common.visual.VisualEffectManager
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.image.ImageLoader
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageAdapter(private val context: Context)
    : BaseListAdapter<ProfileImageVO, BaseProfileImageViewHolder>(ProfileImageDiffCallback()),
    ListPreloader.PreloadModelProvider<ProfileImageVO> {

    var onBeforeLikeListener: (() -> Boolean)? = null
    var tabsObserver: RecyclerView.AdapterDataObserver? = null
    var isLikeEnabled = true

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): BaseProfileImageViewHolder =
        ProfileImageViewHolder(view, isLikeEnabled).also { initLikeButtonClickListener(it) }

    override fun instantiateHeaderViewHolder(view: View) = HeaderProfileImageViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterProfileImageViewHolder(view)

    override fun onBindViewHolder(holder: BaseProfileImageViewHolder, position: Int) {
        holder.setOnClickListener(getOnItemClickListener(holder))
        initLikeButtonClickListener(holder)
        super.onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: BaseProfileImageViewHolder, position: Int, payloads: List<Any>) {
        holder.setOnClickListener(getOnItemClickListener(holder))
        initLikeButtonClickListener(holder)
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getExposedCb(): (() -> Unit)? = { tabsObserver?.onChanged() }

    // --------------------------------------------------------------------------------------------
    override fun getStubItem(): ProfileImageVO = EmptyProfileImageVO

    // ------------------------------------------
    override fun getPreloadItems(position: Int): MutableList<ProfileImageVO> =
        getModels().subList(position, position + 1).toMutableList()

    override fun getPreloadRequestBuilder(item: ProfileImageVO): RequestBuilder<*>? =
        ImageLoader.loadRequest(uri = item.image.uri, context = context,
            options = RequestOptions().centerCrop())

    // ------------------------------------------
    @Suppress("CheckResult")
    private fun initLikeButtonClickListener(vh: BaseProfileImageViewHolder) {
        vh.itemView.ibtn_like.clicks().compose(clickDebounce())
            .subscribe { getOnLikeButtonClickListener(vh).onClick(vh.itemView.ibtn_like) }
    }

    override fun getOnItemClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener {
        val clickListener = if (!isLikeEnabled) {
            super.getOnItemClickListener(vh)
        } else {
            wrapOnItemClickListener(vh, getLikeClickListener(vh, alwaysLiked = true))
        }
        // TODO: only click should be recorded
        // detect touch on image item and call visual effect at touch point
        vh.itemView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                VisualEffectManager.call(LikeVisualEffect(event.x, event.y))
            }
            false
        }
        return clickListener
    }

    private fun getOnLikeButtonClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener =
        wrapOnItemClickListener(vh) { model: ProfileImageVO, position: Int ->
            if (onBeforeLikeListener?.invoke() != false) {
                if (!model.isLiked) {  // was liked
                    notifyItemChanged(vh.adapterPosition, ProfileImageViewHolderAnimateLikeButton)
                } else {   // was unliked
                    notifyItemChanged(vh.adapterPosition, ProfileImageViewHolderAnimateUnLikeButton)
                }
                getLikeClickListener(vh)?.invoke(model, position)
            }
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
                }
            }
}
