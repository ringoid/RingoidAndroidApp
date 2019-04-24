package com.ringoid.base.view.advanced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ringoid.base.manager.permission.IPermissionCaller
import com.ringoid.base.manager.permission.PermissionManager
import com.ringoid.base.view.BaseFragment
import com.ringoid.base.viewmodel.advanced.BasePermissionViewModel
import com.ringoid.domain.debug.DebugLogUtil
import javax.inject.Inject

abstract class BasePermissionFragment<T : BasePermissionViewModel> : BaseFragment<T>() {

    @Inject protected lateinit var permissionManager: PermissionManager

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
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

        override fun onDenied() {
            DebugLogUtil.w("Location permission has been denied")
        }
    }
}
