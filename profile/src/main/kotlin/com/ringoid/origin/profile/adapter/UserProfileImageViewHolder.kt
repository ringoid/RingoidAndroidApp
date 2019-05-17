package com.ringoid.origin.profile.adapter

import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.model.image.UserImage
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.image.ImageLoader
import kotlinx.android.synthetic.main.rv_item_user_profile_image.view.*


abstract class BaseUserProfileImageViewHolder(view: View) : BaseViewHolder<UserImage>(view)

class UserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    init {
        itemView.tv_image_id.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: UserImage) {
        showControls()  // cancel any effect caused by applied payloads
        ImageLoader.load(uri = model.uri, thumbnailUri = model.uriLocal ?: model.thumbnailUri,
            imageView = itemView.iv_image, options = RequestOptions().centerCrop())

        itemView.tv_likes_count.text = "${model.numberOfLikes}"

        if (BuildConfig.IS_STAGING) {
            itemView.tv_image_id.text = "Image: ${model.idWithFirstN()}"
        }
    }

    override fun bind(model: UserImage, payloads: List<Any>) {
        if (payloads.contains(UserProfileImageViewHolderHideControls)) {
            showControls(isVisible = false)
        }
        if (payloads.contains(UserProfileImageViewHolderShowControls)) {
            showControls()
        }
    }

    private fun showControls(isVisible: Boolean = true) {
//        itemView.iv_likes.changeVisibility(isVisible = isVisible)
//        itemView.tv_likes_count.changeVisibility(isVisible = isVisible)
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
