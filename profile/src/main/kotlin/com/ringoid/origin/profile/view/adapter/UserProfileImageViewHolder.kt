package com.ringoid.origin.profile.view.adapter

import android.view.View
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.image.UserImage
import com.ringoid.utility.ImageLoader
import kotlinx.android.synthetic.main.rv_item_user_profile_image.view.*

open class UserProfileImageViewHolder(view: View) : BaseViewHolder<UserImage>(view) {

    override fun bind(model: UserImage) {
        ImageLoader.load(uri = model.uri, imageView = itemView.iv_image,
            options = RequestOptions()
                .override(itemView.width, itemView.height)
                .centerCrop())
    }
}

class HeaderUserProfileImageViewHolder(view: View) : UserProfileImageViewHolder(view) {

    override fun bind(model: UserImage) {
        // no-op
    }
}
