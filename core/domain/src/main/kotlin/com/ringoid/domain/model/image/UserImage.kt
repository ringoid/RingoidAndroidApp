package com.ringoid.domain.model.image

import android.os.Parcel
import android.os.Parcelable
import com.ringoid.domain.DomainUtil
import com.ringoid.utility.randomString

data class UserImage(
    val originId: String = DomainUtil.BAD_ID,
    val numberOfLikes: Int = 0,
    val isBlocked: Boolean = false,
    val sortPosition: Int = DomainUtil.BAD_POSITION,
    val uriLocal: String? = null,
    override val id: String, override val uri: String? = null,
    override val thumbnailUri: String? = null,
    override val isRealModel: Boolean = true) : IImage {

    private constructor(source: Parcel): this(
        id = source.readString() ?: DomainUtil.BAD_ID, uri = source.readString(), thumbnailUri = source.readString(),
        originId = source.readString() ?: DomainUtil.BAD_ID, numberOfLikes = source.readInt(),
        isBlocked = source.readInt() != 0, sortPosition = source.readInt(), uriLocal = source.readString())

    override fun copyWithId(id: String): IImage =
        UserImage(originId = originId, numberOfLikes = numberOfLikes, isBlocked = isBlocked,
                  id = id, uri = uri, thumbnailUri = thumbnailUri)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(uri)
            writeString(thumbnailUri)
            writeString(originId)
            writeInt(numberOfLikes)
            writeInt(if (isBlocked) 1 else 0)
            writeInt(sortPosition)
            writeString(uriLocal)
        }
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<UserImage> {
            override fun createFromParcel(source: Parcel): UserImage = UserImage(source)
            override fun newArray(size: Int): Array<UserImage?> = arrayOfNulls(size)
        }

        fun from(image: IImage): UserImage = UserImage(id = image.id, uri = image.uri, thumbnailUri = image.thumbnailUri)
    }
}

val EmptyUserImage = UserImage(id = randomString(), uri = null, thumbnailUri = null, isRealModel = false)
