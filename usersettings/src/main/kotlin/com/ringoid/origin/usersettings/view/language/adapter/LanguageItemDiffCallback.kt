package com.ringoid.origin.usersettings.view.language.adapter

import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.domain.misc.LanguageItem

class LanguageItemDiffCallback : BaseDiffCallback<LanguageItem>() {

    override fun areItemsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean =
        oldItem.language == newItem.language

    override fun areContentsTheSame(oldItem: LanguageItem, newItem: LanguageItem): Boolean =
        oldItem == newItem
}
