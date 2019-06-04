package com.ringoid.origin.usersettings.view.profile

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.observe
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.origin.error.handleOnView
import com.ringoid.origin.model.*
import com.ringoid.origin.usersettings.OriginR_string
import com.ringoid.origin.usersettings.R
import com.ringoid.origin.usersettings.WidgetR_attrs
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.utility.*
import com.ringoid.widget.model.ListItem
import com.ringoid.widget.view.item_view.textChanges
import kotlinx.android.synthetic.main.fragment_settings_profile.*
import kotlinx.android.synthetic.main.fragment_settings_push.pb_loading
import kotlinx.android.synthetic.main.fragment_settings_push.toolbar

class SettingsProfileFragment : BaseFragment<SettingsProfileViewModel>() {

    companion object {
        internal const val TAG = "SettingsProfileFragment_tag"

        fun newInstance(): SettingsProfileFragment = SettingsProfileFragment()
    }

    override fun getVmClass(): Class<SettingsProfileViewModel> = SettingsProfileViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_settings_profile

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        fun onIdleState() {
            pb_loading.changeVisibility(isVisible = false, soft = true)
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
                item_profile_property_education.setSelectedItem(it.education.reduceForLocale(app?.localeManager?.getLang()))
                item_profile_property_hair_color.setSelectedItem(it.hairColor.valueForGender(spm.currentUserGender()))
                item_profile_property_income.setSelectedItem(it.income)
                item_profile_property_property.setSelectedItem(it.property)
                item_profile_property_transport.setSelectedItem(it.transport)
                item_profile_property_height.setInputText(if (it.height > 0) "${it.height}" else "")
            }
        }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(OriginR_string.settings_profile)
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
        with (item_profile_property_height) {
            clicks().compose(clickDebounce()).subscribe {
                Dialogs.showEditTextDialog(activity, titleResId = OriginR_string.profile_property_height,
                    positiveBtnLabelResId = OriginR_string.button_done,
                    negativeBtnLabelResId = OriginR_string.button_cancel,
                    positiveListener = { dialog, _, text ->
                        val heightStr = handleInputHeight(text).takeIf { it > 0 }?.toString() ?: ""
                        this.setInputText(heightStr)
                        onHeightTextChange(heightStr)
                        dialog.dismiss()
                    },
                    initText = getText(), inputType = InputType.TYPE_CLASS_NUMBER, maxLength = 3)
            }
            textChanges().compose(inputDebounce()).subscribe(::onHeightTextChange)
        }
        with (tv_support) {
            highlightFrom(start = text.lastIndexOf(' '), textColor = context.getAttributeColor(WidgetR_attrs.refTextColorPrimary))
        }
    }

    // ------------------------------------------
    private fun handleInputHeight(it: String?): Int =
        if (it.isNullOrBlank()) 0 else it.toInt().takeIf { int -> int in 92..214 } ?: 0

    private fun onHeightTextChange(heightStr: CharSequence) {
        vm.onPropertyChanged_height(height = if (heightStr.isNotBlank()) heightStr.toString().toInt() else 0)
    }
}
