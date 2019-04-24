package com.ringoid.origin.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ringoid.base.manager.permission.IPermissionCaller
import com.ringoid.base.manager.permission.PermissionManager
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.view.ViewState
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.origin.R
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import javax.inject.Inject

abstract class BasePermissionFragment<T : BasePermissionViewModel> : BaseFragment<T>() {

    @Inject protected lateinit var permissionManager: PermissionManager

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE ->
                when (newState.residual) {
                    is ASK_TO_ENABLE_LOCATION_SERVICE -> askToEnableLocationService()
                }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        registerPermissionCaller(PermissionManager.RC_PERMISSION_LOCATION, locationPermissionCaller)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterPermissionCaller(PermissionManager.RC_PERMISSION_LOCATION, locationPermissionCaller)
    }

    /* Permission handling */
    // --------------------------------------------------------------------------------------------
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    private fun registerPermissionCaller(rc: Int, caller: IPermissionCaller?) {
        permissionManager.registerPermissionCaller(rc, caller)
    }

    private fun unregisterPermissionCaller(rc: Int, caller: IPermissionCaller?) {
        permissionManager.unregisterPermissionCaller(rc, caller)
    }

    /* Permission */
    // --------------------------------------------------------------------------------------------
    private val locationPermissionCaller = LocationPermissionCaller()

    private inner class LocationPermissionCaller : IPermissionCaller {

        @SuppressWarnings("MissingPermission")
        override fun onGranted() {
            DebugLogUtil.i("Location permission has been granted")
            vm.onLocationPermissionGranted()
        }

        override fun onDenied(): Boolean {
            DebugLogUtil.w("Location permission has been denied")
            return false
        }

        override fun onShowRationale() {
            Dialogs.showTextDialog(activity, titleResId = R.string.permission_location_rationale, descriptionResId = 0)
        }
    }

    private fun askToEnableLocationService() {
        if (spm.getLocation() != null) {
            return  // already has location, no need ask to enable gps
        }

        Dialogs.showTextDialog(activity, titleResId = R.string.permission_location_dialog_enable_location_service, descriptionResId = 0,
            positiveBtnLabelResId = R.string.button_settings,
            negativeBtnLabelResId = R.string.button_later,
            positiveListener = { _, _ -> ExternalNavigator.openLocationSettings(activity!!) })
    }
}
