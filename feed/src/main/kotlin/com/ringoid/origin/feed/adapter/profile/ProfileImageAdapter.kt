package com.ringoid.origin.feed.adapter.profile

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.EmptyProfileImageVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageAdapter : BaseListAdapter<ProfileImageVO, BaseProfileImageViewHolder>(ProfileImageDiffCallback()) {

    var tabsObserver: RecyclerView.AdapterDataObserver? = null
    var isLikeEnabled = true

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): BaseProfileImageViewHolder =
        ProfileImageViewHolder(view, isLikeEnabled).also { vh ->
            vh.itemView.ibtn_like.clicks().compose(clickDebounce()).subscribe { getOnLikeButtonClickListener(vh).onClick(vh.itemView.ibtn_like) }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderProfileImageViewHolder(view)

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
    override fun getOnItemClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener =
        if (!isLikeEnabled) {
            super.getOnItemClickListener(vh)
        } else {
            val clickListener = wrapOnItemClickListener(vh, getLikeClickListener(vh, alwaysLiked = true))
//        vh.itemView.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                vh.itemView.iv_like_anim.apply {
//                    x = event.x - width
//                    y = event.y - height
//                }
//            }
//            false
//        }
        clickListener
    }

    private fun getOnLikeButtonClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener =
        wrapOnItemClickListener(vh) { model: ProfileImageVO, position: Int ->
            if (!model.isLiked) {  // was liked
                notifyItemChanged(vh.adapterPosition, ProfileImageViewHolderAnimateLikeButton)
            } else {   // was unliked
                notifyItemChanged(vh.adapterPosition, ProfileImageViewHolderAnimateUnLikeButton)
            }
            getLikeClickListener(vh)?.invoke(model, position)
        }

    private fun getLikeClickListener(vh: BaseProfileImageViewHolder, alwaysLiked: Boolean = false)
        : ((model: ProfileImageVO, position: Int) -> Unit)? =
            { model: ProfileImageVO, position: Int ->
                val isLiked = if (alwaysLiked) true else !model.isLiked
                model.isLiked = isLiked
                if (isLiked) {  // animate on Like, don't animate on Unlike
                    notifyItemChanged(vh.adapterPosition, ProfileImageViewHolderAnimateLike)
                }
                itemClickListener?.invoke(model, position)
            }
}
