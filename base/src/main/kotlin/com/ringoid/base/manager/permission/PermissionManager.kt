package com.ringoid.base.manager.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ringoid.utility.priorVersion
import com.ringoid.utility.targetVersion
import dagger.Reusable
import javax.inject.Inject

@Reusable
class PermissionManager @Inject constructor() {

    companion object {
        const val RC_PERMISSION_LOCATION = 801
    }

    private val callers = mutableMapOf<Int, MutableList<IPermissionCaller?>>()

    fun askForLocationPermission(activity: Activity) {
        askForPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, RC_PERMISSION_LOCATION)
    }

    fun askForLocationPermission(fragment: Fragment) {
        askForPermission(fragment, Manifest.permission.ACCESS_FINE_LOCATION, RC_PERMISSION_LOCATION)
    }

    // ------------------------------------------
    internal fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callers[requestCode]?.forEach { it?.onGranted() }
        } else {
            callers[requestCode]?.forEach { it?.onDenied() }
        }
    }

    internal fun registerPermissionCaller(rc: Int, caller: IPermissionCaller?) {
        if (!callers.containsKey(rc)) {
            callers[rc] = mutableListOf()
        }

        callers[rc]?.add(caller)
    }

    internal fun unregisterPermissionCaller(rc: Int, caller: IPermissionCaller?) {
        callers[rc]?.remove(caller)
    }

    // --------------------------------------------------------------------------------------------
    private fun hasPermission(context: Context, permission: String): Boolean =
        priorVersion(Build.VERSION_CODES.M) ||
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun askForPermission(activity: Activity, permission: String, rc: Int) {
        if (hasPermission(activity, permission)) {
            callers[rc]?.forEach { it?.onGranted() }
            return
        }

        if (targetVersion(Build.VERSION_CODES.M)) {
            activity.requestPermissions(arrayOf(permission), rc)
        }
    }

    private fun askForPermission(fragment: Fragment, permission: String, rc: Int) {
        if (hasPermission(fragment.activity!!, permission)) {
            callers[rc]?.forEach { it?.onGranted() }
            return
        }

        if (targetVersion(Build.VERSION_CODES.M)) {
            fragment.requestPermissions(arrayOf(permission), rc)
        }
    }
}
