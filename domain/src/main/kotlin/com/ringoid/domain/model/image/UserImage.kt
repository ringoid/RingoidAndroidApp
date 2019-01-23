package com.ringoid.domain.model.image

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil.BAD_ID
import com.ringoid.utility.randomString

data class UserImage(
    val originId: String = BAD_ID, val numberOfLikes: Int = 0,
    override val id: String, override val uri: String? = null) : IImage {

    private constructor(source: Parcel): this(
        id = source.readString() ?: BAD_ID, uri = source.readString(),
        originId = source.readString() ?: BAD_ID, numberOfLikes = source.readInt())

    override fun copyWithId(id: String): IImage =
        UserImage(originId = originId, numberOfLikes = numberOfLikes, id = id, uri = uri)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(uri)
            writeString(originId)
            writeInt(numberOfLikes)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<UserImage> {
            override fun createFromParcel(source: Parcel): UserImage = UserImage(source)
            override fun newArray(size: Int): Array<UserImage?> = arrayOfNulls(size)
        }

        fun from(image: IImage): UserImage = UserImage(id = image.id, uri = image.uri)
    }
}

val EmptyUserImage = UserImage(id = randomString(), uri = null)
