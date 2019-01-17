package com.ringoid.origin.feed.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageAdapter : BaseListAdapter<ProfileImageVO, ProfileImageViewHolder>(ProfileImageDiffCallback()) {

    var tabsObserver: RecyclerView.AdapterDataObserver? = null

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): ProfileImageViewHolder =
        ProfileImageViewHolder(view).apply {
            itemView.ibtn_like.clicks().compose(clickDebounce())
                .subscribe { getOnItemClickListener(this).onClick(itemView.ibtn_like) }
        }

    override fun submitList(list: List<ProfileImageVO>?) {
        super.submitList(list)
        tabsObserver?.onChanged()
    }

    // --------------------------------------------------------------------------------------------
    override fun getOnItemClickListener(vh: ProfileImageViewHolder): View.OnClickListener {
        val xl: ((model: ProfileImageVO, position: Int) -> Unit)? = { model: ProfileImageVO, position: Int ->
            val isLiked = !model.isLiked
            vh.animateLike(isLiked = isLiked)
            model.isLiked = isLiked
            itemClickListener?.invoke(model, position)
        }
        return super.wrapOnItemClickListener(vh, xl)
    }
}

// ------------------------------------------------------------------------------------------------
class ProfileImageDiffCallback : BaseDiffCallback<ProfileImageVO>() {

    override fun areItemsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean = oldItem.image.id == newItem.image.id

    override fun areContentsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean = oldItem == newItem  // as 'data class'
}
