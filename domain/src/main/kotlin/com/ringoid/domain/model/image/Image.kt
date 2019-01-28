package com.ringoid.domain.model.image

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.utility.randomString

data class Image(override val id: String, override val uri: String? = null,
                 override val isRealModel: Boolean = true) : IImage {

    private constructor(source: Parcel): this(id = source.readString() ?: BAD_ID, uri = source.readString())

    override fun copyWithId(id: String): IImage = Image(id = id, uri = uri)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(uri)
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

val EmptyImage = Image(id = randomString(), uri = null, isRealModel = false)
