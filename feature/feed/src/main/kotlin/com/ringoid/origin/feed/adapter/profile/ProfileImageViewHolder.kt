package com.ringoid.origin.feed.adapter.profile

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.BuildConfig
import com.ringoid.imageloader.ImageLoader
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_profile_image.view.*

abstract class BaseProfileImageViewHolder(view: View) : BaseViewHolder<ProfileImageVO>(view)

class ProfileImageViewHolder(view: View) : BaseProfileImageViewHolder(view) {

    init {
        itemView.tv_image_id.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    @Suppress("SetTextI18n")
    override fun bind(model: ProfileImageVO) {
        ImageLoader.load(uri = model.image.uri, thumbnailUri = model.image.thumbnailUri,
                         iv = itemView.iv_image, extra = listOf("profileId" to model.profileId))

        if (BuildConfig.IS_STAGING) {
            itemView.tv_image_id.text = "Image: ${model.image.idWithFirstN()}"
        }
    }

    override fun cleanUp() {
        super.cleanUp()
        with (itemView.iv_image) {
            controller?.onDetach()
            topLevelDrawable?.callback = null
        }
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
