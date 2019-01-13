package com.ringoid.origin.feed.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.image.Image
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

class ProfileImageViewHolder(view: View) : BaseViewHolder<Image>(view) {

    override fun bind(model: Image) {
        ImageLoader.load(model.uri, itemView.iv_image)
    }
}
