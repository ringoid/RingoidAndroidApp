package com.ringoid.base.view

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ViewState : Parcelable {

    @Parcelize object NO_STATE : ViewState()  // used for initialization of LiveData
    @Parcelize object IDLE : ViewState()
    @Parcelize object LOADING : ViewState()
    @Parcelize object PAGING : ViewState()

    // ------------------------------------------
    data class CLEAR(val mode: Int = MODE_DEFAULT) : ViewState() {

        private constructor(source: Parcel): this(mode = source.readInt())

        companion object {
            const val MODE_DEFAULT = 0
            const val MODE_EMPTY_DATA = 1
            const val MODE_NEED_REFRESH = 2
            const val MODE_CHANGE_FILTERS = 3

            @JvmField
            val CREATOR = object : Parcelable.Creator<CLEAR> {
                override fun createFromParcel(source: Parcel): CLEAR = CLEAR(source)
                override fun newArray(size: Int): Array<CLEAR?> = arrayOfNulls(size)
            }
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(mode)
        }
    }

    // ------------------------------------------
    data class ERROR(val e: Throwable) : ViewState() {

        private constructor(source: Parcel): this(e = source.readSerializable() as Throwable)

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<ERROR> {
                override fun createFromParcel(source: Parcel): ERROR = ERROR(source)
                override fun newArray(size: Int): Array<ERROR?> = arrayOfNulls(size)
            }
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeSerializable(e)
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {}

    override fun toString(): String = javaClass.simpleName
}
