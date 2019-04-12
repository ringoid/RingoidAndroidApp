package com.ringoid.main.view

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.google.firebase.iid.FirebaseInstanceId
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.observe
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

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> =
            listOf(
                LmmFragment.newInstance(),
                UserProfileFragment.newInstance(),
                ExploreFragment.newInstance())

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
            setContainerView(findViewById(OriginR_id.fl_container))
            addGenerator(LikesParticleGenerator(this@MainActivity))
            addGenerator(MatchesParticleGenerator(this@MainActivity))
            addGenerator(MessagesParticleGenerator(this@MainActivity))
        }
    }
}
