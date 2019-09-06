package com.ringoid.origin.usersettings.view.language

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.view.BaseActivity
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.usersettings.WidgetR_attrs
import com.ringoid.origin.usersettings.view.language.adapter.LanguageItemVO
import com.ringoid.origin.usersettings.view.language.adapter.SettingsLangAdapter
import com.ringoid.origin.usersettings.view.language.adapter.SettingsLangViewHolderIsChecked
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.getAttributeColor
import com.ringoid.utility.highlightFrom
import com.ringoid.utility.manager.LocaleManager
import kotlinx.android.synthetic.main.fragment_settings_language.*

class SettingsLangFragment : BaseFragment<SettingsLangViewModel>() {

    companion object {
        internal const val TAG = "LanguageFragment_tag"

        fun newInstance(): SettingsLangFragment = SettingsLangFragment()
    }

    private val langAdapter = SettingsLangAdapter().apply {
        itemClickListener = { model, _ ->
            app?.localeManager?.setNewLocale(context!!, lang = model.language.id)
            vm.updateUserSettingLocale()  // viewModel outlives this screen's recreation
            (activity as? BaseActivity<*>)?.apply {
                setResultExposed(Activity.RESULT_OK)
                recreate()
            }
        }
    }

    override fun getVmClass(): Class<SettingsLangViewModel> = SettingsLangViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_language

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_LANGUAGE

    // --------------------------------------------------------------------------------------------
    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setTitle(OriginR_string.settings_language)
            navigationClicks().compose(clickDebounce()).subscribe { activity?.onBackPressed() }
        }

        rv_items.apply {
            adapter = langAdapter
            layoutManager = LinearLayoutManager(context)
        }
        with (tv_lang_help) {
            highlightFrom(start = text.lastIndexOf(' '), textColor = context.getAttributeColor(WidgetR_attrs.refTextColorPrimary))
        }
    }

    override fun onStart() {
        super.onStart()
        langAdapter.submitList(listOf(
            LanguageItemVO(LocaleManager.LANG_EN),
            LanguageItemVO(LocaleManager.LANG_RU)))

        // restore selected language item
        app?.localeManager?.getLang()?.let { langId ->
            langAdapter.findModelAndPosition { it.language.id == langId }
                ?.let { (position, model) ->
                    model.toggleSelected()
                    langAdapter.notifyItemChanged(position, SettingsLangViewHolderIsChecked)
                }
        }
    }
}
