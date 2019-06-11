package com.ringoid.origin.feed.adapter.profile

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.image.ImageLoader
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

abstract class BaseProfileImageViewHolder(view: View) : BaseViewHolder<ProfileImageVO>(view) {

    open fun cleanUp() {
        // no-op
    }
}

class ProfileImageViewHolder(view: View) : BaseProfileImageViewHolder(view) {

    init {
        itemView.tv_image_id.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: ProfileImageVO) {
        ImageLoader.load(uri = model.image.uri, thumbnailUri = null/*model.image.thumbnailUri*/, imageView = itemView.iv_image)

        if (BuildConfig.IS_STAGING) {
            itemView.tv_image_id.text = "Image: ${model.image.idWithFirstN()}"
        }
    }

    override fun cleanUp() {
        super.cleanUp()
        itemView.iv_image.setImageDrawable(null)
    }
}

class HeaderProfileImageViewHolder(view: View) : BaseProfileImageViewHolder(view) {

    override fun bind(model: ProfileImageVO) {
        // no-op
    }
}

class FooterProfileImageViewHolder(view: View) : BaseProfileImageViewHolder(view) {

    override fun bind(model: ProfileImageVO) {
        // no-op
    }
}
