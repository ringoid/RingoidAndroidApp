package com.ringoid.origin.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ringoid.base.manager.permission.IPermissionCaller
import com.ringoid.base.manager.permission.PermissionManager
import com.ringoid.base.observeOneShot
import com.ringoid.base.view.BaseFragment
import com.ringoid.debug.DebugLogUtil
import com.ringoid.origin.R
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import timber.log.Timber
import javax.inject.Inject

abstract class BasePermissionFragment<T : BasePermissionViewModel> : BaseFragment<T>() {

    @Inject protected lateinit var permissionManager: PermissionManager

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        registerPermissionCaller(PermissionManager.RC_PERMISSION_LOCATION, locationPermissionCaller)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        observeOneShot(vm.askToEnableLocationServiceOneShot(), ::askToEnableLocationService)
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
            Dialogs.showTextDialog(activity, titleResId = R.string.permission_location_rationale, descriptionResId = 0)
        }
    }

    private fun askToEnableLocationService(handleCode: Int) {
        Timber.v("Ask to enable location service: hc=$handleCode")
        Dialogs.showTextDialog(activity, titleResId = R.string.permission_location_dialog_enable_location_service, descriptionResId = 0,
            positiveBtnLabelResId = R.string.button_settings,
            negativeBtnLabelResId = R.string.button_later,
            positiveListener = { _, _ -> ExternalNavigator.openLocationSettingsForResult(this@BasePermissionFragment) },
            isCancellable = false)

        onAskToEnableLocationService()
    }

    protected open fun onAskToEnableLocationService() {
        // sie-effect upon asking to enable location service, override in subclasses
    }
}
