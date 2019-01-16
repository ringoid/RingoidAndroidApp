package com.ringoid.origin.feed.adapter

import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageAdapter : BaseListAdapter<ProfileImageVO, ProfileImageViewHolder>(ProfileImageDiffCallback()) {

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): ProfileImageViewHolder =
        ProfileImageViewHolder(view).apply {
            itemView.ibtn_like.clicks().compose(clickDebounce())
                .subscribe { getOnItemClickListener(this).onClick(itemView.ibtn_like) }
        }

    // --------------------------------------------------------------------------------------------
    override fun wrapOnItemClickListener(vh: ProfileImageViewHolder, l: ((model: ProfileImageVO, position: Int) -> Unit)?): View.OnClickListener {
        val xl: ((model: ProfileImageVO, position: Int) -> Unit)? = { model: ProfileImageVO, position: Int ->
            vh.animateLike(isLiked = !model.isLiked)
            l?.invoke(model, position)
        }
        return super.wrapOnItemClickListener(vh, xl)
    }
}

// ------------------------------------------------------------------------------------------------
class ProfileImageDiffCallback : BaseDiffCallback<ProfileImageVO>() {

    override fun areItemsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean = oldItem.image.id == newItem.image.id

    override fun areContentsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean = oldItem == newItem  // as 'data class'
}
