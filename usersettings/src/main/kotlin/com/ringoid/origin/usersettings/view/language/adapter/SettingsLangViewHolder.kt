package com.ringoid.origin.usersettings.view.language.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.misc.LanguageItem
import kotlinx.android.synthetic.main.rv_item_settings_lang.view.*

class SettingsLangViewHolder(view: View) : BaseViewHolder<LanguageItem>(view) {

    override fun bind(model: LanguageItem) {
        itemView.tv_lang_label.text = model.language
    }
}
