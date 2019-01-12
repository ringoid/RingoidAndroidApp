package com.ringoid.origin.view.adapter.feed

import android.view.View
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.image.Image
import com.ringoid.origin.R

class ProfileImageAdapter : BaseListAdapter<Image, ProfileImageViewHolder>(ProfileImageDiffCallback()) {

    override fun getLayoutId(): Int = R.layout.rv_item_profile_image

    override fun instantiateViewHolder(view: View): ProfileImageViewHolder = ProfileImageViewHolder(view)
}

// ------------------------------------------------------------------------------------------------
class ProfileImageDiffCallback : BaseDiffCallback<Image>() {

    override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean =
        oldItem == newItem  // as 'data class'
}
