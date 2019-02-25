package com.ringoid.origin.usersettings.view.language.adapter

import android.view.View
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.origin.utils.LocaleUtils
import com.ringoid.utility.changeVisibility
import kotlinx.android.synthetic.main.rv_item_settings_lang.view.*

open class SettingsLangViewHolder(view: View) : BaseViewHolder<LanguageItemVO>(view) {

    override fun bind(model: LanguageItemVO) {
        itemView.apply {
            iv_check.changeVisibility(isVisible = model.isSelected)
            tv_lang_label.text = LocaleUtils.getLangById(context, langId = model.language.id)
        }
    }

    override fun bind(model: LanguageItemVO, payloads: List<Any>) {
        super.bind(model, payloads)
        if (payloads.contains(SettingsLangViewHolderIsChecked)) {
            itemView.iv_check.changeVisibility(isVisible = true)
        }
        if (payloads.contains(SettingsLangViewHolderUnChecked)) {
            itemView.iv_check.changeVisibility(isVisible = false)
        }
    }
}

class HeaderSettingsLangViewHolder(view: View): SettingsLangViewHolder(view) {

    override fun bind(model: LanguageItemVO) {
        // no-op
    }

    override fun bind(model: LanguageItemVO, payloads: List<Any>) {
        // no-op
    }
}
