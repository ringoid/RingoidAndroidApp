package com.ringoid.origin.usersettings.view.language.adapter

sealed class SettingsLangViewHolderPayload

object SettingsLangViewHolderIsChecked : SettingsLangViewHolderPayload()
object SettingsLangViewHolderUnChecked : SettingsLangViewHolderPayload()
