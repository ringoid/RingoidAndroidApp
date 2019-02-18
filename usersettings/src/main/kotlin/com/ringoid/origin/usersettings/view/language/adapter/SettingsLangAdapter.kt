package com.ringoid.origin.usersettings.view.language.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ringoid.domain.misc.LanguageItem
import com.ringoid.origin.usersettings.R

class SettingsLangAdapter : ListAdapter<LanguageItem, SettingsLangViewHolder>(LanguageItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsLangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_settings_lang, parent, false)
        return SettingsLangViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingsLangViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
