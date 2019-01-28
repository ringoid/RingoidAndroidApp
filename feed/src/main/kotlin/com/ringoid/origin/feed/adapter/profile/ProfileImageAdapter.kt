package com.ringoid.origin.feed.adapter.profile

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.EmptyProfileImageVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageAdapter : BaseListAdapter<ProfileImageVO, BaseProfileImageViewHolder>(ProfileImageDiffCallback()) {

    var tabsObserver: RecyclerView.AdapterDataObserver? = null
    var isLikeButtonVisible = true

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): BaseProfileImageViewHolder =
        ProfileImageViewHolder(view).also { vh ->
            vh.itemView.ibtn_like.apply {
                changeVisibility(isVisible = isLikeButtonVisible)
                clicks().compose(clickDebounce()).subscribe { getOnLikeButtonClickListener(vh).onClick(vh.itemView.ibtn_like) }
            }
        }

    override fun instantiateHeaderViewHolder(view: View) = HeaderProfileImageViewHolder(view)

    override fun getExposedCb(): (() -> Unit)? = { tabsObserver?.onChanged() }

    // --------------------------------------------------------------------------------------------
    override fun getHeaderItem(): ProfileImageVO = EmptyProfileImageVO

    // ------------------------------------------
    override fun getOnItemClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener {
        val clickListener = super.wrapOnItemClickListener(vh, getLikeClickListener(vh, setAlwaysLiked = true))
//        vh.itemView.setOnTouchListener { _, event ->
//            if (event.action == MotionEvent.ACTION_DOWN) {
//                vh.itemView.iv_like_anim.apply {
//                    x = event.x - width
//                    y = event.y - height
//                }
//            }
//            false
//        }
        return clickListener
    }

    private fun getOnLikeButtonClickListener(vh: BaseProfileImageViewHolder): View.OnClickListener =
        super.wrapOnItemClickListener(vh, getLikeClickListener(vh))

    private fun getLikeClickListener(vh: BaseProfileImageViewHolder, setAlwaysLiked: Boolean = false)
        : ((model: ProfileImageVO, position: Int) -> Unit)? =
            { model: ProfileImageVO, position: Int ->
                val isLiked = if (setAlwaysLiked) true else !model.isLiked
                vh.animateLike(isLiked = isLiked)
                model.isLiked = isLiked
                itemClickListener?.invoke(model, position)
            }
}

// ------------------------------------------------------------------------------------------------
class ProfileImageDiffCallback : BaseDiffCallback<ProfileImageVO>() {

    override fun areItemsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean = oldItem.image.id == newItem.image.id

    override fun areContentsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean = oldItem == newItem  // as 'data class'
}
