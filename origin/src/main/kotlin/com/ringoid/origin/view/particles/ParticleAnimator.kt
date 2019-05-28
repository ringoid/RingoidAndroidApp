package com.ringoid.origin.view.particles

import android.app.Activity
import android.view.ViewGroup
import android.view.animation.Interpolator
import com.github.jinatonic.confetti.ConfettiManager
import com.github.jinatonic.confetti.ConfettiSource
import com.github.jinatonic.confetti.confetto.Confetto
import com.ringoid.base.isInPowerSafeMode
import com.ringoid.origin.AppRes
import dagger.Reusable
import java.util.*
import javax.inject.Inject

const val PARTICLE_TYPE_LIKE = "particle_type_like"
const val PARTICLE_TYPE_MATCH = "particle_type_match"
const val PARTICLE_TYPE_MESSAGE = "particle_type_message"

@Reusable
class ParticleAnimator @Inject constructor() {

    @Inject lateinit var random: Random
    private lateinit var context: Activity
    private lateinit var containerView: ViewGroup
    private val generators = mutableMapOf<String, ParticleGenerator>()
    private val managers = mutableListOf<Pair<ConfettiManager, Boolean>>()

    fun init(activity: Activity) {
        context = activity
    }

    fun addGenerator(generator: ParticleGenerator) {
        generators[generator.id] = generator
    }

    fun animate(id: String, count: Int = 1) {
        animateN(id, count)
    }

    private fun animateN(id: String, count: Int) {
        if (context.isInPowerSafeMode()) {
            return
        }
        if (count <= 0 || !generators.containsKey(id)) {
            return
        }

        val duration = count / 2 * 1000L
        val generator = generators[id]!!
//        val source = ConfettiSource(containerView.width / 3 - AppRes.ICON_SIZE_36 - AppRes.STD_MARGIN_8, containerView.height - AppRes.MAIN_BOTTOM_BAR_HEIGHT - AppRes.ICON_SIZE_36)
        val source = ConfettiSource(containerView.width / 2 - AppRes.STD_MARGIN_16, containerView.height - AppRes.MAIN_BOTTOM_BAR_HEIGHT - AppRes.ICON_SIZE_36)

        ConfettiManager(containerView.context, generator, source, containerView)
            .setEmissionDuration(duration)
            .setEmissionRate(2f)
            .setNumInitialCount(1)
            .setVelocityX(generator.velocityX, generator.velocityDevX)
            .setAccelerationX(generator.accelerationX, generator.accelerationDevX)
            .setTargetVelocityX(generator.targetVelocityX, generator.targetVelocityDevX)
            .setVelocityY(generator.velocityY, generator.velocityDevY)
//            .setAccelerationY(generator.accelerationY, generator.accelerationDevY)
            .setTargetVelocityY(generator.targetVelocityY, generator.targetVelocityDevY)
            .setTTL(generator.ttl)
            .setConfettiAnimationListener(object : ConfettiManager.ConfettiAnimationListener {
                override fun onAnimationStart(confettiManager: ConfettiManager) { managers.add(confettiManager to true) }
                override fun onAnimationEnd(confettiManager: ConfettiManager) {
                    managers.indexOfFirst { it.first == confettiManager && it.second }
                        .takeIf { it != -1 }
                        ?.let { managers[it] = managers[it].first to false }
                }
                override fun onConfettoEnter(confetto: Confetto?) {}
                override fun onConfettoExit(confetto: Confetto?) {}
            })
            .enableFadeOut(ParticleInterpolator(random.nextFloat() * 0.1f))
            .animate()
    }

    fun setContainerView(containerView: ViewGroup) {
        this.containerView = containerView
    }

    fun terminate() {
        managers
            .filter { it.second }
            .forEach {
                it.first.let {
                    it.setConfettiAnimationListener(null)
                    it.terminate()
                }
            }
        managers.clear()
    }
}

internal class ParticleInterpolator(private val randomStart: Float) : Interpolator {

    override fun getInterpolation(input: Float): Float = maxOf(0f, 1 - randomStart - input)
}
