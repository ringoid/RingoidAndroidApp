package com.ringoid.widget.model

import androidx.annotation.StringRes

interface IListItem {

    val id: Int
    @StringRes fun getLabelResId(): Int
}
