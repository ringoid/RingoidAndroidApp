package com.ringoid.origin

import android.content.Context
import com.ringoid.domain.misc.ImageResolution

object ScreenHelper {

    fun getLargestPossibleImageResolution(context: Context): ImageResolution =
        context.resources.displayMetrics.widthPixels.let { w ->
            val resolution: ImageResolution = ImageResolution._480x640
            ImageResolution.values().reversedArray().forEach {
                if (resolution.w <= w) {
                    return@let it
                }
            }
            resolution
        }
}
