package com.ringoid.origin.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.IEssence

data class BlockReportPayload(
    @Expose @SerializedName(COLUMN_PROFILE_IMAGE_URI) val profileImageUri: String? = null,
    @Expose @SerializedName(COLUMN_PROFILE_THUMB_URI) val profileThumbnailUri: String? = null)
    : IEssence, Parcelable {

    private constructor(source: Parcel)
            : this(profileImageUri = source.readString(),
                   profileThumbnailUri = source.readString())

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with (dest) {
            writeString(profileImageUri)
            writeString(profileThumbnailUri)
        }
    }

    companion object {
        const val COLUMN_PROFILE_IMAGE_URI = "profileImageUri"
        const val COLUMN_PROFILE_THUMB_URI = "profileThumbUri"

        @JvmField
        val CREATOR = object : Parcelable.Creator<BlockReportPayload> {
            override fun createFromParcel(source: Parcel): BlockReportPayload = BlockReportPayload(source)
            override fun newArray(size: Int): Array<BlockReportPayload?> = arrayOfNulls(size)
        }
    }
}
