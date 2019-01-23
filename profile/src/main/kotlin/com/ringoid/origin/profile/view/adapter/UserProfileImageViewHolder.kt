package com.ringoid.origin.profile.view.adapter

import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.image.UserImage
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.rv_item_user_profile_image.view.*

interface IUserProfileImageViewHolder

abstract class BaseUserProfileImageViewHolder(view: View) : BaseViewHolder<UserImage>(view), IUserProfileImageViewHolder

class UserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    override fun bind(model: UserImage, payloads: List<Any>) {
        ImageLoader.load(uri = model.uri, imageView = itemView.iv_image,
            options = RequestOptions()
                .override(itemView.width, itemView.height)
                .centerCrop())
    }
}

class HeaderUserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    override fun bind(model: UserImage, payloads: List<Any>) {
        // no-op
    }
}
