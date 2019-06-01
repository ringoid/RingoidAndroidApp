package com.ringoid.widget.model

import androidx.annotation.StringRes

interface IListItem {

    val id: Int
    val isDefault: Boolean
    @StringRes fun getLabelResId(): Int
}
