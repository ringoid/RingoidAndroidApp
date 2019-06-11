package com.ringoid.utility.image

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager

class ImageRequest(context: Context) {

    internal val imageLoader: RequestManager = Glide.with(context)
}
