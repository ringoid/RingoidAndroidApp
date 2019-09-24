package com.ringoid.origin.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

data class FeedItemContextMenuPayload(
    @Expose @SerializedName(COLUMN_PROFILE_IMAGE_URI) val profileImageUri: String? = null,
    @Expose @SerializedName(COLUMN_PROFILE_THUMB_URI) val profileThumbnailUri: String? = null,
    @Expose @SerializedName(COLUMN_SOCIAL_INSTAGRAM) val socialInstagram: String? = null,
    @Expose @SerializedName(COLUMN_SOCIAL_TIKTOK) val socialTiktok: String? = null)
    : IEssence, Parcelable {

    private constructor(source: Parcel)
            : this(profileImageUri = source.readString(),
                   profileThumbnailUri = source.readString(),
                   socialInstagram = source.readString(),
                   socialTiktok = source.readString())

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with (dest) {
            writeString(profileImageUri)
            writeString(profileThumbnailUri)
            writeString(socialInstagram)
            writeString(socialTiktok)
        }
    }

    companion object {
        const val COLUMN_PROFILE_IMAGE_URI = "profileImageUri"
        const val COLUMN_PROFILE_THUMB_URI = "profileThumbUri"
        const val COLUMN_SOCIAL_INSTAGRAM = "socialInstagram"
        const val COLUMN_SOCIAL_TIKTOK = "socialTiktok"

        @JvmField
        val CREATOR = object : Parcelable.Creator<FeedItemContextMenuPayload> {
            override fun createFromParcel(source: Parcel): FeedItemContextMenuPayload = FeedItemContextMenuPayload(source)
            override fun newArray(size: Int): Array<FeedItemContextMenuPayload?> = arrayOfNulls(size)
        }
    }
}
