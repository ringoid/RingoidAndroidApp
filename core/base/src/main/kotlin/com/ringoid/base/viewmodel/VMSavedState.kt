package com.ringoid.base.viewmodel

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.ringoid.base.view.ViewState

internal data class VMSavedState(val viewState: ViewState) : Parcelable {

    private constructor(source: Parcel): this(viewState = source.readParcelable(ViewState::class.java.classLoader))

    companion object {
        private const val BUNDLE_KEY_VIEW_STATE = "bundle_key_view_state"

        @JvmField
        val CREATOR = object : Parcelable.Creator<VMSavedState> {
            override fun createFromParcel(source: Parcel): VMSavedState = VMSavedState(source)
            override fun newArray(size: Int): Array<VMSavedState?> = arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(viewState, flags)
    }

    internal fun saveToBundle(bundle: Bundle) {
        bundle.putParcelable(BUNDLE_KEY_VIEW_STATE, viewState)
    }
}
