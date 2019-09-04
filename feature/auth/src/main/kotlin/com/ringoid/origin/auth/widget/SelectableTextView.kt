package com.ringoid.origin.auth.widget

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.TextView

/**
 * For saving View state:
 *
 * @see https://trickyandroid.com/saving-android-view-state-correctly/?source=post_page-----9dbe96074d49----------------------
 * @see https://medium.com/@kirillsuslov/how-to-save-android-view-state-in-kotlin-9dbe96074d49
 */
class SelectableTextView : TextView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) : this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int) : super(context, attributes, defStyleAttr) {
        isClickable = true
    }

    override fun onRestoreInstanceState(state: Parcelable) =
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            this@SelectableTextView.isSelected = state.isSelected
        } else {
            super.onRestoreInstanceState(state)
        }

    override fun onSaveInstanceState(): Parcelable? =
        super.onSaveInstanceState()?.let {
            SavedState(it).apply {
                isSelected = this@SelectableTextView.isSelected
            }
        }

    // --------------------------------------------------------------------------------------------
    internal class SavedState : androidx.customview.view.AbsSavedState {

        var isSelected: Boolean = false

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel) : super(source) {
            isSelected = source.readByte().toInt() != 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)  // avoid ClassNotFoundException when unmarshalling: <some name>
            dest.writeByte((if (isSelected) 1 else 0).toByte())
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}
