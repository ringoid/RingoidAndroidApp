package com.ringoid.origin.usersettings.view.language.adapter

import com.ringoid.base.adapter.BaseDiffCallback

class LanguageItemDiffCallback : BaseDiffCallback<LanguageItemVO>() {

    override fun areItemsTheSame(oldItem: LanguageItemVO, newItem: LanguageItemVO): Boolean =
        oldItem.language.id == newItem.language.id

    override fun areContentsTheSame(oldItem: LanguageItemVO, newItem: LanguageItemVO): Boolean =
        oldItem.language == newItem.language
}
