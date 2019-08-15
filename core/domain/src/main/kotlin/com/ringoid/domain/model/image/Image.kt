package com.ringoid.domain.model.image

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil
import com.ringoid.utility.randomString

data class Image(override val id: String, override val uri: String? = null,
                 override val thumbnailUri: String? = null,
                 override val isRealModel: Boolean = true) : IImage {

    private constructor(source: Parcel): this(id = source.readString() ?: DomainUtil.BAD_ID,
        uri = source.readString(), thumbnailUri = source.readString())

    override fun copyWithId(id: String): IImage = Image(id = id, uri = uri, thumbnailUri = thumbnailUri)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(uri)
            writeString(thumbnailUri)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<Image> {
            override fun createFromParcel(source: Parcel): Image = Image(source)
            override fun newArray(size: Int): Array<Image?> = arrayOfNulls(size)
        }
    }
}

val EmptyImage = Image(id = randomString(), uri = null, thumbnailUri = null, isRealModel = false)
