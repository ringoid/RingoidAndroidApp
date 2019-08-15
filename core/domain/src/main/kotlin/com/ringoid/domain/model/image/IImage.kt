package com.ringoid.domain.model.image

import android.os.Parcelable
import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.IModel

interface IImage : IModel, Parcelable {

    val uri: String?
    val thumbnailUri: String?

    fun copyWithId(id: String): IImage

    fun getImageIdStr(): String = id.indexOf('_').takeIf { it != -1 }?.let { id.substring(it + 1) } ?: id
    fun getResolutionStr(): String? = id.indexOf('_').takeIf { it != -1 }?.let { id.substring(0, it) }
    fun getResolution(): ImageResolution? = getResolutionStr()?.let { str -> ImageResolution.values().find { it.resolution == str } }
    fun hashIdWithResolution(): String = "${getResolutionStr()}_${getImageIdStr().substring(0..3)}_${getModelId()}"

    override fun idWithFirstN(N: Int): String = getImageIdStr().substring(0..minOf(N, id.length - 1))
}
