package com.ringoid.origin.usersettings.view.profile

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.model.*
import com.ringoid.origin.usersettings.*
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.usersettings.view.base.BaseSettingsFragment
import com.ringoid.origin.view.dialog.BigEditTextDialog
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.*
import com.ringoid.widget.model.ListItem
import com.ringoid.widget.view.item_view.TextIconItemView
import com.ringoid.widget.view.item_view.textChanges
import kotlinx.android.synthetic.main.fragment_settings_profile.*
import kotlinx.android.synthetic.main.fragment_settings_push.pb_loading
import kotlinx.android.synthetic.main.fragment_settings_push.toolbar
import leakcanary.AppWatcher

class SettingsProfileFragment : BaseSettingsFragment<SettingsProfileViewModel>(),
    BigEditTextDialog.IBigEditTextDialogDone {

    companion object {
        internal const val TAG = "SettingsProfileFragment_tag"
        private const val ABOUT_PROPERTY_DIALOG_TAG = "PropertyAbout"

        fun newInstance(): SettingsProfileFragment = SettingsProfileFragment()
    }

    override fun getVmClass(): Class<SettingsProfileViewModel> = SettingsProfileViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_profile

    override fun appScreen(): AppScreen = AppScreen.SETTINGS_PROFILE

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_loading?.changeVisibility(isVisible = false, soft = true)
        }

        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.IDLE -> onIdleState()
            is ViewState.LOADING -> pb_loading.changeVisibility(isVisible = true)
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState)
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (viewLifecycleOwner) {
            observe(vm.profile) {
                // properties
                item_profile_property_children.setSelectedItem(it.children)
                item_profile_property_education.setSelectedItem(it.education.reduceForLocale(app?.localeManager?.getLang()))
                item_profile_property_hair_color.setSelectedItem(it.hairColor.valueForGender(spm.currentUserGender()))
                item_profile_property_income.setSelectedItem(it.income)
                item_profile_property_property.setSelectedItem(it.property)
                item_profile_property_transport.setSelectedItem(it.transport)
                // custom properties
                item_profile_custom_property_about.setInputText(it.about().trim())
                item_profile_custom_property_company.setInputText(it.company())
                item_profile_custom_property_job_title.setInputText(it.jobTitle())
                item_profile_property_height.setInputText(if (it.height > 0) "${it.height}" else "")
                item_profile_custom_property_name.setInputText(it.name())
                item_profile_custom_property_instagram.setInputText(it.instagram())
                item_profile_custom_property_tiktok.setInputText(it.tiktok())
                item_profile_custom_property_university.setInputText(it.university())
                item_profile_custom_property_where_live.setInputText(it.whereLive())
            }
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            inflateMenu(OriginR_menu.menu_done)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    OriginR_id.menu_done -> { activity?.onBackPressed(); true }
                    else -> false
                }
            }
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_profile)
        }

        // properties
        with (item_profile_property_children) {
            setItems(ChildrenProfileProperty.values.toList())
            setOnItemSelectedListener<ChildrenProfileProperty> { vm.onPropertyChanged_children(it) }
        }
        with (item_profile_property_education) {
            setItems(EducationProfileProperty.valuesForLocale(app?.localeManager?.getLang()))
            setOnItemSelectedListener<EducationProfileProperty> { vm.onPropertyChanged_education(it) }
        }
        with (item_profile_property_hair_color) {
            setItems(HairColorProfileProperty.valuesForGender(spm.currentUserGender()))
            setOnItemSelectedListener<ListItem> { vm.onPropertyChanged_hairColor(HairColorProfileProperty.from(it)) }
        }
        with (item_profile_property_income) {
            setItems(IncomeProfileProperty.values.toList())
            setOnItemSelectedListener<IncomeProfileProperty> { vm.onPropertyChanged_income(it) }
        }
        with (item_profile_property_property) {
            setItems(PropertyProfileProperty.values.toList())
            setOnItemSelectedListener<PropertyProfileProperty> { vm.onPropertyChanged_property(it) }
        }
        with (item_profile_property_transport) {
            setItems(TransportProfileProperty.values.toList())
            setOnItemSelectedListener<TransportProfileProperty> { vm.onPropertyChanged_transport(it) }
        }

        // custom properties
        with (item_profile_custom_property_about) {
            clicks().compose(clickDebounce()).subscribe {
                BigEditTextDialog.newInstance(titleResId = OriginR_string.settings_profile_item_custom_property_about,
                    btnPositiveResId = OriginR_string.button_done,
                    btnNegativeResId = OriginR_string.button_cancel,
                    input = getText(), tag = ABOUT_PROPERTY_DIALOG_TAG)
                .show(childFragmentManager, BigEditTextDialog.TAG)
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onAboutTextChange)
        }
        with (item_profile_custom_property_company) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.settings_profile_item_custom_property_company,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        if (this.setInputText(text)) onCompanyTextChange(text)
                        dialog.dismiss()
                    },
                    initText = getText(),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    maxLength = TextIconItemView.MAX_LENGTH,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onCompanyTextChange)
        }
        with (item_profile_custom_property_job_title) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.settings_profile_item_custom_property_job_title,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        if (this.setInputText(text)) onJobTitleTextChange(text)
                        dialog.dismiss()
                    },
                    initText = getText(),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    maxLength = TextIconItemView.MAX_LENGTH,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onJobTitleTextChange)
        }
        with (item_profile_property_height) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.profile_property_height,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        val heightStr = handleInputHeight(text).takeIf { it > 0 }?.toString() ?: ""
                        if (this.setInputText(heightStr)) onHeightTextChange(heightStr)
                        dialog.dismiss()
                    },
                    initText = getText(),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    maxLength = 3,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onHeightTextChange)
            setSuffix(OriginR_string.value_cm)
        }
        with (item_profile_custom_property_name) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.settings_profile_item_custom_property_name,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        if (this.setInputText(text)) onNameTextChange(text)
                        dialog.dismiss()
                    },
                    initText = getText(),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    maxLength = 20,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onNameTextChange)
        }
        with (item_profile_custom_property_instagram) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.settings_profile_item_custom_property_instagram,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        if (this.setInputText(text)) onSocialInstagramTextChange(text)
                        dialog.dismiss()
                    },
                    initText = getText(), maxLength = TextIconItemView.MAX_LENGTH,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onSocialInstagramTextChange)
        }
        with (item_profile_custom_property_tiktok) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.settings_profile_item_custom_property_tiktok,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        if (this.setInputText(text)) onSocialTikTokTextChange(text)
                        dialog.dismiss()
                    },
                    initText = getText(), maxLength = TextIconItemView.MAX_LENGTH,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onSocialTikTokTextChange)
        }
        with (item_profile_custom_property_university) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.settings_profile_item_custom_property_university,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        if (this.setInputText(text)) onUniversityTextChange(text)
                        dialog.dismiss()
                    },
                    initText = getText(),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    maxLength = TextIconItemView.MAX_LENGTH,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onUniversityTextChange)
        }
        with (item_profile_custom_property_where_live) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.settings_profile_item_custom_property_where_live,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        if (this.setInputText(text)) onWhereLiveTextChange(text)
                        dialog.dismiss()
                    },
                    initText = getText(),
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    maxLength = TextIconItemView.MAX_LENGTH,
                    imeOptions = EditorInfo.IME_ACTION_DONE)
                .also { AppWatcher.objectWatcher.watch(it) }
            }
            textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onWhereLiveTextChange)
        }

        // other
        item_suggest_improvements.clicks().compose(clickDebounce()).subscribe { openSuggestImprovementsDialog("SuggestFromProfileSettings") }
        with (tv_support) {
            highlightFrom(start = text.lastIndexOf(' '), textColor = context.getAttributeColor(WidgetR_attrs.refTextColorPrimary))
        }
    }

    // --------------------------------------------------------------------------------------------
    private fun handleInputHeight(it: String?): Int =
        if (it.isNullOrBlank()) 0 else it.toInt().takeIf { int -> int in 92..214 } ?: 0

    override fun onCancel(text: String, tag: String?) {
        if (tag != ABOUT_PROPERTY_DIALOG_TAG) {
            super.onCancel(text, tag)
        }
    }

    override fun onDone(text: String, tag: String?) {
        if (tag != ABOUT_PROPERTY_DIALOG_TAG) {
            super.onDone(text, tag)
        } else if (item_profile_custom_property_about.setInputText(text)) {
            onAboutTextChange(text)
        }
    }

    // ------------------------------------------
    private fun onAboutTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_about(text = it.toString().trim()) }
    }

    private fun onCompanyTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_company(text = it.toString().trim()) }
    }

    private fun onJobTitleTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_jobTitle(text = it.toString().trim()) }
    }

    private fun onHeightTextChange(heightStr: CharSequence) {
        vm.onCustomPropertyChanged_height(height = if (heightStr.isNotBlank()) heightStr.toString().toInt() else 0)
    }

    private fun onNameTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_name(text = it.toString().trim()) }
    }

    private fun onSocialInstagramTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_socialInstagram(text = it.toString().trim()) }
    }

    private fun onSocialTikTokTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_socialTikTok(text = it.toString().trim()) }
    }

    private fun onUniversityTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_university(text = it.toString().trim()) }
    }

    private fun onWhereLiveTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_whereLive(text = it.toString().trim()) }
    }
}
