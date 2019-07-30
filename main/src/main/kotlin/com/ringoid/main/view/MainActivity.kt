package com.ringoid.main.view

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import com.google.firebase.iid.FirebaseInstanceId
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.observe
import com.ringoid.base.view.ViewState
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.main.OriginR_id
import com.ringoid.main.OriginR_string
import com.ringoid.main.OriginR_style
import com.ringoid.main.listOfMainScreens
import com.ringoid.origin.AppRes
import com.ringoid.origin.utils.AppUtils
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.main.BaseMainActivity
import com.ringoid.origin.view.particles.*

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    private var currentLocale: String? = null
    @StyleRes private var currentThemeResId: Int = 0

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> = listOfMainScreens()

    // --------------------------------------------------------------------------------------------
    override fun onViewStateChange(newState: ViewState) {
        super.onViewStateChange(newState)
        when (newState) {
            is ViewState.DONE ->
                when (newState.residual) {
                    is CLOSE_DEBUG_VIEW -> askToCloseDebugView()
                }
        }
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLocale = app.localeManager.getLang()
        currentThemeResId = spm.getThemeResId(defaultThemeResId = OriginR_style.AppTheme_Dark)
        observe(vm.badgeLikes, ::showBadgeOnLikes)
        observe(vm.badgeMessages, ::showBadgeOnMessages)
        observe(vm.badgeWarningProfile, ::showBadgeWarningOnProfile)
        observe(vm.countLikes, ::showCountOnLikes)
        observe(vm.countMessages, ::showCountOnMessages)
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
            AppRes.initTranslatableStrings(resources)  // translate strings if locale has changed
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
            init(this@MainActivity)
            setContainerView(findViewById(OriginR_id.fl_container))
            addGenerator(LikesParticleGenerator(this@MainActivity))
            addGenerator(MatchesParticleGenerator(this@MainActivity))
            addGenerator(MessagesParticleGenerator(this@MainActivity))
        }
    }

    // ------------------------------------------
    override fun transferProfile(profileId: String, payload: Bundle?) {
        // TODO: transferProfile
    }

    // ------------------------------------------
    @DebugOnly
    private fun askToCloseDebugView() {
        Dialogs.showTextDialog(this, titleResId = OriginR_string.dialog_debug_view_close_title,
                               descriptionResId = OriginR_string.dialog_debug_view_close_description,
                               positiveBtnLabelResId = OriginR_string.button_ok,
                               negativeBtnLabelResId = OriginR_string.button_cancel,
                               positiveListener = { _, _ ->
                                   spm.enableDebugLog(isEnabled = false)
                                   showDebugView()
                               })
    }
}
