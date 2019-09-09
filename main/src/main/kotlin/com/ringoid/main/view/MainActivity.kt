package com.ringoid.main.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.iid.FirebaseInstanceId
import com.ringoid.base.deeplink.AppNav
import com.ringoid.base.eventbus.Bus
import com.ringoid.base.eventbus.BusEvent
import com.ringoid.base.observe
import com.ringoid.base.observeOneShot
import com.ringoid.main.OriginR_id
import com.ringoid.main.OriginR_string
import com.ringoid.main.listOfMainScreens
import com.ringoid.origin.AppRes
import com.ringoid.origin.utils.AppUtils
import com.ringoid.origin.view.dialog.Dialogs
import com.ringoid.origin.view.main.BaseMainActivity
import com.ringoid.origin.view.particles.*
import com.ringoid.utility.DebugOnly
import com.ringoid.utility.theme.ThemeId

@AppNav("main")
class MainActivity : BaseMainActivity<MainViewModel>() {

    companion object {
        private const val BUNDLE_KEY_FLAG_MARK_FOR_RECREATION = "bundle_key_flag_mark_for_recreation"
    }

    private var currentLocale: String? = null
    private var currentTheme: ThemeId = ThemeId.UNKNOWN

    private var markForRecreation: Boolean = false

    override fun getVmClass() = MainViewModel::class.java

    override fun getListOfRootFragments(): List<Fragment> = listOfMainScreens()

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentLocale = app.localeManager.getLang()
        currentTheme = spm.getThemeId(defaultTheme = ThemeId.DARK)
        observe(vm.badgeLikes(), ::showBadgeOnLikes)
        observe(vm.badgeMessages(), ::showBadgeOnMessages)
        observe(vm.badgeWarningProfile(), ::showBadgeWarningOnProfile)
        observe(vm.newLikesCount()) { showParticleAnimation(id = PARTICLE_TYPE_LIKE, count = it) }
        observe(vm.newMatchesCount()) { showParticleAnimation(id = PARTICLE_TYPE_MATCH, count = it) }
        observe(vm.newMessagesCount()) { showParticleAnimation(id = PARTICLE_TYPE_MESSAGE, count = it) }
        observeOneShot(vm.closeDebugViewOneShot()) { askToCloseDebugView() }

        AppUtils.checkForGooglePlayServices(this)
        initializeFirebase()
        initializeParticleAnimation()

        savedInstanceState?.let {
            markForRecreation = it.getBoolean(BUNDLE_KEY_FLAG_MARK_FOR_RECREATION, false)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (isStopped) {
            vm.onAppReOpen()
        }
    }

    override fun onStart() {
        super.onStart()
        if (markForRecreation) {
            markForRecreation = false
            Bus.post(BusEvent.RecreateMainScreen)
        }
        if (currentLocale != app.localeManager.getLang() ||
            currentTheme != spm.getThemeId(defaultTheme = currentTheme)) {
            AppRes.initTranslatableStrings(resources)  // translate strings if locale has changed
            markForRecreation = true
            recreate()  // locale or theme has changed outside, in some another Activity
        }
    }

    override fun onResume() {
        super.onResume()
        AppUtils.checkForGooglePlayServices(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BUNDLE_KEY_FLAG_MARK_FOR_RECREATION, markForRecreation)
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
