package com.ringoid.origin.feed.adapter.base

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.domain.model.feed.Profile

class ProfileDiffCallback : BaseDiffCallback<Profile>() {

    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean =
        oldItem == newItem  // as 'data class'
}
