package com.ringoid.origin.profile.adapter

import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.image.UserImage
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.rv_item_user_profile_image.view.*


abstract class BaseUserProfileImageViewHolder(view: View) : BaseViewHolder<UserImage>(view)

class UserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    override fun bind(model: UserImage) {
        ImageLoader.load(uri = model.uri, imageView = itemView.iv_image,
            options = RequestOptions()
                .override(itemView.width, itemView.height)
                .centerCrop())

        itemView.tv_likes_count.text = "${model.numberOfLikes}"  // TODO: use payload
    }
}

class HeaderUserProfileImageViewHolder(view: View) : BaseUserProfileImageViewHolder(view) {

    override fun bind(model: UserImage) {
        // no-op
    }
}
