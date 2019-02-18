package com.ringoid.origin.usersettings.view.language.adapter

import com.ringoid.domain.misc.LanguageItem
import com.ringoid.domain.model.IListModel

data class LanguageItemVO(val language: LanguageItem, var isSelected: Boolean = false) : IListModel {

    constructor(language: String, isSelected: Boolean = false): this(language = LanguageItem(language), isSelected = isSelected)

    override fun getModelId(): Long = language.hashCode().toLong()

    fun toggleSelected() {
        isSelected = !isSelected
    }
}

val EmptyLanguageItemVO = LanguageItemVO(language = LanguageItem(""), isSelected = false)
