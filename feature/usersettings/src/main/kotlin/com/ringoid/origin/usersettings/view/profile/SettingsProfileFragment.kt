package com.ringoid.origin.usersettings.view.profile

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.appcompat.itemClicks
import com.jakewharton.rxbinding3.appcompat.navigationClicks
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.navigation.AppScreen
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.misc.UserProfileEditablePropertyId
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.model.*
import com.ringoid.origin.usersettings.*
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.view.base.settings.BaseSettingsFragment
import com.ringoid.origin.view.dialog.BigEditTextDialog
import com.ringoid.utility.*
import com.ringoid.widget.model.ListItem
import com.ringoid.widget.view.item_view._textChanges
import kotlinx.android.synthetic.main.fragment_settings_profile.*
import kotlinx.android.synthetic.main.fragment_settings_push.pb_loading
import kotlinx.android.synthetic.main.fragment_settings_push.toolbar

class SettingsProfileFragment : BaseSettingsFragment<SettingsProfileViewModel>(),
    BigEditTextDialog.IBigEditTextDialogDone {

    private var isInitialFocus: Boolean = true

    companion object {
        internal const val TAG = "SettingsProfileFragment_tag"
        private const val ABOUT_PROPERTY_DIALOG_TAG = "PropertyAbout"

        private const val BUNDLE_KEY_FOCUS_FIELD = "bundle_key_focus_field"

        fun newInstance(focus: String? = null): SettingsProfileFragment =
            SettingsProfileFragment().apply {
                arguments = Bundle().apply { putString(BUNDLE_KEY_FOCUS_FIELD, focus) }
            }
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
            is ViewState.ERROR -> newState.e.handleOnView(this, ::onIdleState) { vm.retryOnError() }
            else -> { /* no-op */ }
        }
    }

    // --------------------------------------------------------------------------------------------
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        with (viewLifecycleOwner) {
            observe(vm.profile()) {
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
                item_profile_custom_property_status.setInputText(it.status())
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
            setTitle(OriginR_string.settings_profile)
            inflateMenu(OriginR_menu.menu_done)
            itemClicks().compose(clickDebounce()).subscribe {
                when (it.itemId) {
                    OriginR_id.menu_done -> activity?.onBackPressed()
                }
            }
            navigationClicks().compose(clickDebounce()).subscribe { activity?.onBackPressed() }
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
                    input = notBlankOf(vm.getCustomPropertyUnsavedInput_about(), getText()),
                    tag = ABOUT_PROPERTY_DIALOG_TAG)
                .show(childFragmentManager, BigEditTextDialog.TAG)
            }
            _textChanges().skipInitialValue().compose(inputDebounce()).subscribe(::onAboutTextChange)
        }
        with (item_profile_custom_property_company) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onCompanyTextChange)
        }
        with (item_profile_custom_property_job_title) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onJobTitleTextChange)
        }
        with (item_profile_property_height) {
            setSuffix(OriginR_string.value_cm)
            _textChanges().skipInitialValue().compose(inputDebounce())
                .map { it.toString() }
                .map { text -> handleInputHeight(text).takeIf { it > 0 }?.toString() ?: "" }
                .subscribe(::onHeightTextChange)
        }
        with (item_profile_custom_property_name) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onNameTextChange)
        }
        with (item_profile_custom_property_status) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onStatusTextChange)
        }
        with (item_profile_custom_property_instagram) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onSocialInstagramTextChange)
        }
        with (item_profile_custom_property_tiktok) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onSocialTikTokTextChange)
        }
        with (item_profile_custom_property_university) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onUniversityTextChange)
        }
        with (item_profile_custom_property_where_live) {
            _textChanges().skipInitialValue().compose(inputDebounceNet()).subscribe(::onWhereLiveTextChange)
        }

        // other
        item_suggest_improvements.clicks().compose(clickDebounce()).subscribe { openSuggestImprovementsDialog("SuggestFromProfileSettings") }
        with (tv_support) {
            highlightFrom(start = text.lastIndexOf(' '), textColor = context.getAttributeColor(WidgetR_attrs.refTextColorPrimary))
        }
    }

    override fun onResume() {
        super.onResume()
        if (isInitialFocus) {
            arguments?.getString(BUNDLE_KEY_FOCUS_FIELD)
                ?.let { UserProfileEditablePropertyId.valueOf(it) }
                ?.let {
                    when (it) {
                        UserProfileEditablePropertyId.COMPANY -> item_profile_custom_property_company.requestFocus()
                        UserProfileEditablePropertyId.JOB_TITLE -> item_profile_custom_property_job_title.requestFocus()
                        UserProfileEditablePropertyId.HEIGHT -> item_profile_property_height.requestFocus()
                        UserProfileEditablePropertyId.NAME -> item_profile_custom_property_name.requestFocus()
                        UserProfileEditablePropertyId.SOCIAL_INSTAGRAM -> item_profile_custom_property_instagram.requestFocus()
                        UserProfileEditablePropertyId.SOCIAL_TIKTOK -> item_profile_custom_property_tiktok.requestFocus()
                        UserProfileEditablePropertyId.STATUS -> item_profile_custom_property_status.requestFocus()
                        UserProfileEditablePropertyId.UNIVERSITY -> item_profile_custom_property_university.requestFocus()
                        UserProfileEditablePropertyId.WHERE_LIVE -> item_profile_custom_property_where_live.requestFocus()
                    }
                } ?: run {
                    // focus on particular fields, if they are empty
                    if (item_profile_custom_property_name.isEmpty()) {
                        item_profile_custom_property_name.requestFocus()
                        return
                    }
                    if (item_profile_custom_property_where_live.isEmpty()) {
                        item_profile_custom_property_where_live.requestFocus()
                        return
                    }
                    if (item_profile_custom_property_status.isEmpty()) {
                        item_profile_custom_property_status.requestFocus()
                        return
                    }
                }
        }
    }

    override fun onPause() {
        super.onPause()
        isInitialFocus = false  // next onResume() won't be treated as initial focus
    }

    override fun onStop() {
        super.onStop()
        activity?.window?.showKeyboard()
    }

    // --------------------------------------------------------------------------------------------
    private fun handleInputHeight(it: String?): Int =
        if (it.isNullOrBlank()) 0 else it.toInt().takeIf { int -> int in 92..214 } ?: 0

    override fun onCancel(text: String, tag: String?, fromBtn: Boolean) {
        if (tag != ABOUT_PROPERTY_DIALOG_TAG) {
            super.onCancel(text, tag, fromBtn)
        } else if (fromBtn) {
            onAboutUnsavedInput(null)
        } else {
            onAboutUnsavedInput(text)
        }
    }

    override fun onDone(text: String, tag: String?) {
        if (tag != ABOUT_PROPERTY_DIALOG_TAG) {
            super.onDone(text, tag)
        } else if (item_profile_custom_property_about.setInputText(text)) {
            onAboutTextChange(text)
        }
        onAboutUnsavedInput(null)
    }

    // ------------------------------------------
    private fun onAboutTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_about(text = it.toString().trim()) }
    }

    private fun onAboutUnsavedInput(text: CharSequence?) {
        vm.onCustomPropertyUnsavedInput_about(text?.toString()?.trim() ?: "")
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

    private fun onStatusTextChange(text: CharSequence?) {
        text?.let { vm.onCustomPropertyChanged_status(text = it.toString().trim()) }
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
