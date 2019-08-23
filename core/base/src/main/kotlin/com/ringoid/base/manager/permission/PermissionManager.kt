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
import javax.inject.Inject

class PermissionManager @Inject constructor() {

    companion object {
        const val HC_NONE = 0
        const val RC_PERMISSION_LOCATION = 810
    }

    private val callers = mutableMapOf<Int, MutableList<IPermissionCaller?>>()

    fun askForLocationPermission(activity: Activity, handleCode: Int = HC_NONE): Boolean =
        askForPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION, RC_PERMISSION_LOCATION, handleCode)

    fun askForLocationPermission(fragment: Fragment, handleCode: Int = HC_NONE): Boolean =
        askForPermission(fragment, Manifest.permission.ACCESS_FINE_LOCATION, RC_PERMISSION_LOCATION, handleCode)

    fun hasLocationPermission(activity: Activity): Boolean =
        hasPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)

    fun hasLocationPermission(fragment: Fragment): Boolean =
        hasPermission(fragment.activity!!, Manifest.permission.ACCESS_FINE_LOCATION)

    // ------------------------------------------
    fun onRequestPermissionsResult(activity: Activity, mergedCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            return
        }

        val handleCode = mergedCode % 10
        val requestCode = mergedCode - handleCode

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callers[requestCode]?.forEach { it?.onGranted(handleCode) }
        } else {
            callers[requestCode]?.forEach {
                if (it?.onDenied(handleCode) == false &&
                    targetVersion(Build.VERSION_CODES.M) &&
                    !activity.shouldShowRequestPermissionRationale(permissions[0])) {
                    it.onShowRationale(handleCode)
                }
            }
        }
    }

    fun onRequestPermissionsResult(fragment: Fragment, mergedCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) {
            return
        }

        val handleCode = mergedCode % 10
        val requestCode = mergedCode - handleCode

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callers[requestCode]?.forEach { it?.onGranted(handleCode) }
        } else {
            callers[requestCode]?.forEach {
                if (it?.onDenied(handleCode) == false &&
                    targetVersion(Build.VERSION_CODES.M) &&
                    !fragment.shouldShowRequestPermissionRationale(permissions[0])) {
                    it.onShowRationale(handleCode)
                }
            }
        }
    }

    fun registerPermissionCaller(rc: Int, caller: IPermissionCaller?) {
        if (!callers.containsKey(rc)) {
            callers[rc] = mutableListOf()
        }

        callers[rc]?.add(caller)
    }

    fun unregisterPermissionCaller(rc: Int, caller: IPermissionCaller?) {
        callers[rc]?.remove(caller)
    }

    // --------------------------------------------------------------------------------------------
    private fun hasPermission(context: Context, permission: String): Boolean =
        priorVersion(Build.VERSION_CODES.M) ||
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun askForPermission(activity: Activity, permission: String, rc: Int, handleCode: Int = HC_NONE): Boolean {
        if (hasPermission(activity, permission)) {
            callers[rc]?.forEach { it?.onGranted(handleCode) }
            return true
        }

        if (targetVersion(Build.VERSION_CODES.M)) {
            activity.requestPermissions(arrayOf(permission), rc + handleCode)
        }
        return false
    }

    private fun askForPermission(fragment: Fragment, permission: String, rc: Int, handleCode: Int = HC_NONE): Boolean {
        if (hasPermission(fragment.activity!!, permission)) {
            callers[rc]?.forEach { it?.onGranted(handleCode) }
            return true
        }

        if (targetVersion(Build.VERSION_CODES.M)) {
            fragment.requestPermissions(arrayOf(permission), rc + handleCode)
        }
        return false
    }
}
