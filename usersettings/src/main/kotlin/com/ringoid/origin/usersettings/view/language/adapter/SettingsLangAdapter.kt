package com.ringoid.origin.usersettings.view.language.adapter

import android.view.View
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.origin.usersettings.R

class SettingsLangAdapter : BaseListAdapter<LanguageItemVO, SettingsLangViewHolder>(LanguageItemDiffCallback()) {

    override fun getLayoutId(): Int = R.layout.rv_item_settings_lang

    override fun instantiateViewHolder(view: View) = SettingsLangViewHolder(view)

    override fun instantiateHeaderViewHolder(view: View) = HeaderSettingsLangViewHolder(view)

    // ------------------------------------------
    override fun getStubItem(): LanguageItemVO = EmptyLanguageItemVO

    // ------------------------------------------
    override fun getOnItemClickListener(vh: SettingsLangViewHolder): View.OnClickListener =
        wrapOnItemClickListener(vh) { model, position ->
            getModel(position).apply {
                if (!isSelected) {
                    findModelAndPosition { it.isSelected }
                        ?.let { (position, model) ->
                            model.toggleSelected()
                            notifyItemChanged(position, SettingsLangViewHolderUnChecked)
                        }
                    toggleSelected()
                    notifyItemChanged(position, SettingsLangViewHolderIsChecked)
                    itemClickListener?.invoke(model, position)
                }
            }
        }
}
