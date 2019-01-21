package com.ringoid.origin.feed.adapter.profile

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageAdapter : BaseListAdapter<ProfileImageVO, ProfileImageViewHolder>(ProfileImageDiffCallback()) {

    var tabsObserver: RecyclerView.AdapterDataObserver? = null
    var isLikeButtonVisible = true

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): ProfileImageViewHolder =
        ProfileImageViewHolder(view).also { vh ->
            vh.itemView.ibtn_like.apply {
                changeVisibility(isVisible = isLikeButtonVisible)
                clicks().compose(clickDebounce()).subscribe { getOnItemClickListener(vh).onClick(vh.itemView.ibtn_like) }
            }
        }

    override fun submitList(list: List<ProfileImageVO>?) {
        super.submitList(list)
        tabsObserver?.onChanged()
    }

    // --------------------------------------------------------------------------------------------
    internal fun getItemExposed(position: Int): ProfileImageVO = getItem(position)
    internal fun getItemsExposed(from: Int, to: Int): List<ProfileImageVO> {
        val list = mutableListOf<ProfileImageVO>()
        for (i in from..to) list.add(getItem(i))
        return list
    }

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
