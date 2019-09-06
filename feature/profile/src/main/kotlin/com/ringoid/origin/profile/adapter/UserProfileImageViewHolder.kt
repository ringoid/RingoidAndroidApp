package com.ringoid.origin.profile.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.model.image.UserImage
import com.ringoid.imageloader.ImageLoader
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_user_profile_image.view.*


abstract class BaseUserProfileImageViewHolder(view: View) : BaseViewHolder<UserImage>(view)

class UserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    init {
        itemView.tv_image_id.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    @Suppress("SetTextI18n")
    override fun bind(model: UserImage) {
        ImageLoader.load(uri = model.uri, thumbnailUri = model.uriLocal ?: model.thumbnailUri,
                         iv = itemView.iv_image, extra = listOf("profileId" to "current user"))

        itemView.tv_likes_count.text = "${model.numberOfLikes}"

        if (BuildConfig.IS_STAGING) {
            itemView.tv_image_id.text = "Image: ${model.idWithFirstN()}"
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

class HeaderUserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    override fun bind(model: UserImage) {
        // no-op
    }
}

class FooterUserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    override fun bind(model: UserImage) {
        // no-op
    }
}
