package com.ringoid.origin.view.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.feed.Profile
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.rv_item_feed_profile.view.*

class ProfileViewHolder(view: View) : BaseViewHolder<Profile>(view) {

    override fun bind(model: Profile) {
        // TODO: change to list of images
        ImageLoader.load(model.images[0].uri, itemView.iv_image)
    }
}
