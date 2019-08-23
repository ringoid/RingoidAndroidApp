package com.ringoid.widget.model

import androidx.annotation.StringRes

interface IListItem {

    val id: Int
    val isDefault: Boolean
    @StringRes fun getLabelResId(): Int
}

data class ListItem(
    override val id: Int,
    override val isDefault: Boolean,
    val l: () -> Int) : IListItem {

    override fun getLabelResId(): Int = l.invoke()
}
