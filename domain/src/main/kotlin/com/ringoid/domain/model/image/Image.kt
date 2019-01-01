package com.ringoid.domain.model.image

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil.BAD_ID

open class Image(val id: String, val uri: String) : Parcelable {

    private constructor(source: Parcel): this(id = source.readString() ?: BAD_ID, uri = source.readString() ?: "")

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
