package com.ringoid.origin.utils

import android.app.Activity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ringoid.origin.navigation.RequestCode

object AppUtils {

    /* Google Play Services */
    // --------------------------------------------------------------------------------------------
    internal interface GmsErrorDialogListener {
        fun onGmsErrorDialogCancel()
    }

    /**
     * Performs check whether Google Play Services are available or enabled on the current device,
     * and shows warning, if not.
     */
    fun checkForGooglePlayServices(activity: Activity): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(activity)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                val dialog = googleAPI.getErrorDialog(activity, result, RequestCode.RC_GOOGLE_PLAY)
                dialog.setOnCancelListener {
                    (activity as? GmsErrorDialogListener)?.onGmsErrorDialogCancel()
                }
                dialog.show()
            }
            return false
        }
        return true
    }
}
