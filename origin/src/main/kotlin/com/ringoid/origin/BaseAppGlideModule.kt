package com.ringoid.origin

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class BaseAppGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setBitmapPool(LruBitmapPool(25 * 1024 * 1024))  // 25 mb
    }
}
