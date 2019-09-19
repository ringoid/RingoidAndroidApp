package com.ringoid.origin.view.base

import android.os.Bundle
import com.ringoid.base.manager.permission.IPermissionCaller
import com.ringoid.base.manager.permission.PermissionManager
import com.ringoid.base.observeOneShot
import com.ringoid.debug.DebugLogUtil
import com.ringoid.origin.R
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.view.base.theme.ThemedBaseActivity
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import timber.log.Timber
import javax.inject.Inject

abstract class BasePermissionActivity<T : BasePermissionViewModel> : ThemedBaseActivity<T>() {

    @Inject protected lateinit var permissionManager: PermissionManager

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerPermissionCaller(PermissionManager.RC_PERMISSION_LOCATION, locationPermissionCaller)
        observeOneShot(vm.askToEnableLocationServiceOneShot(), ::askToEnableLocationService)
    }

    override fun onDestroy() {
        super.onDestroy()
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
        override fun onGranted(handleCode: Int) {
            DebugLogUtil.i("Location permission has been granted")
            vm.onLocationPermissionGranted(handleCode)
        }

        override fun onDenied(handleCode: Int): Boolean {
            DebugLogUtil.w("Location permission has been denied")
            vm.onLocationPermissionDenied(handleCode)
            return false
        }

        override fun onShowRationale(handleCode: Int) {
            Dialogs.showTextDialog(this@BasePermissionActivity, titleResId = R.string.permission_location_rationale, descriptionResId = 0)
        }
    }

    private fun askToEnableLocationService(handleCode: Int) {
        Timber.v("Ask to enable location service: hc=$handleCode")
        Dialogs.showTextDialog(this, titleResId = R.string.permission_location_dialog_enable_location_service, descriptionResId = 0,
            positiveBtnLabelResId = R.string.button_settings,
            negativeBtnLabelResId = R.string.button_later,
            positiveListener = { _, _ -> ExternalNavigator.openLocationSettingsForResult(this@BasePermissionActivity) },
            isCancellable = false)
    }
}
