package com.ringoid.main.view

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.google.firebase.iid.FirebaseInstanceId
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.manager.permission.IPermissionCaller
import com.ringoid.base.manager.permission.PermissionManager
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.main.OriginR_id
import com.ringoid.main.OriginR_style
import com.ringoid.origin.feed.view.explore.ExploreFragment
import com.ringoid.origin.feed.view.lmm.LmmFragment
import com.ringoid.origin.profile.view.UserProfileFragment
import com.ringoid.origin.utils.AppUtils
import com.ringoid.origin.view.main.BaseMainActivity
import com.ringoid.origin.view.particles.*

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    private var currentLocale: String? = null
    @StyleRes private var currentThemeResId: Int = 0

    private val locationPermissionCaller = LocationPermissionCaller()

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> =
            listOf(
                LmmFragment.newInstance(),
                UserProfileFragment.newInstance(),
                ExploreFragment.newInstance())

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE -> {
                when (newState.residual) {
                    is ASK_LOCATION_PERMISSION -> permissionManager.askForLocationPermission(this)
                }
            }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLocale = app.localeManager.getLang()
        currentThemeResId = spm.getThemeResId(defaultThemeResId = OriginR_style.AppTheme_Dark)
        observe(vm.badgeLmm, ::showBadgeOnLmm)
        observe(vm.badgeWarningProfile, ::showBadgeWarningOnProfile)
        observe(vm.newLikesCount) { showParticleAnimation(id = PARTICLE_TYPE_LIKE, count = it) }
        observe(vm.newMatchesCount) { showParticleAnimation(id = PARTICLE_TYPE_MATCH, count = it) }
        observe(vm.newMessagesCount) { showParticleAnimation(id = PARTICLE_TYPE_MESSAGE, count = it) }

        AppUtils.checkForGooglePlayServices(this)
        initializeFirebase()
        initializeParticleAnimation()
        registerPermissionCaller(PermissionManager.RC_PERMISSION_LOCATION, locationPermissionCaller)
    }

    override fun onStart() {
        super.onStart()
        if (currentLocale != app.localeManager.getLang() ||
            currentThemeResId != spm.getThemeResId(defaultThemeResId = currentThemeResId)) {
            recreate()  // locale or theme has changed outside, in some another Activity
        }
    }

    override fun onResume() {
        super.onResume()
        AppUtils.checkForGooglePlayServices(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterPermissionCaller(PermissionManager.RC_PERMISSION_LOCATION, locationPermissionCaller)
    }

    // --------------------------------------------------------------------------------------------
    private fun initializeFirebase() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener {
                it.takeIf { it.isSuccessful }
                    ?.result?.token
                    ?.let { vm.updatePushToken(it) }
            }
    }

    private fun initializeParticleAnimation() {
        with(particleAnimator) {
            init(this@MainActivity)
            setContainerView(findViewById(OriginR_id.fl_container))
            addGenerator(LikesParticleGenerator(this@MainActivity))
            addGenerator(MatchesParticleGenerator(this@MainActivity))
            addGenerator(MessagesParticleGenerator(this@MainActivity))
        }
    }

    /* Permission */
    // --------------------------------------------------------------------------------------------
    private inner class LocationPermissionCaller : IPermissionCaller {

        @SuppressWarnings("MissingPermission")
        override fun onGranted() {
            DebugLogUtil.i("Location permission has been granted")
            (getSystemService(Context.LOCATION_SERVICE) as? LocationManager)
                ?.let {
                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            vm.onLocationChanged(latitude = location.latitude, longitude = location.longitude)
                        }
                        override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {}
                        override fun onProviderDisabled(provider: String) {}
                        override fun onProviderEnabled(provider: String) {}
                    }
                    it.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, listener)
                }
        }

        override fun onDenied() {
            DebugLogUtil.w("Location permission has been denied")
        }
    }
}
