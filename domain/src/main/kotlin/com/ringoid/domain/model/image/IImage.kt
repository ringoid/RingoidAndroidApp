package com.ringoid.domain.model.image

import android.os.Parcelable
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.IModel

interface IImage : IModel, Parcelable {

    val uri: String?

    fun copyWithId(id: String): IImage

    fun getResolutionStr(): String? = id.indexOf('_').takeIf { it != -1 }?.let { id.substring(0, it) }
    fun getResolution(): ImageResolution? = getResolutionStr()?.let { str -> ImageResolution.values().find { it.resolution == str } }
    fun hashIdWithResolution(): String = "${getResolutionStr()}_${id.substring(0..4)}_${getModelId()}"
}
