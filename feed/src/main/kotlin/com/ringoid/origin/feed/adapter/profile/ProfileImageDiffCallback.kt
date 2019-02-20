package com.ringoid.origin.feed.adapter.profile

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.origin.feed.model.ProfileImageVO

class ProfileImageDiffCallback : BaseDiffCallback<ProfileImageVO>() {

    override fun areItemsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean = oldItem.image.id == newItem.image.id

    override fun areContentsTheSame(oldItem: ProfileImageVO, newItem: ProfileImageVO): Boolean =
        oldItem.image == newItem.image  // as 'data class'
}
