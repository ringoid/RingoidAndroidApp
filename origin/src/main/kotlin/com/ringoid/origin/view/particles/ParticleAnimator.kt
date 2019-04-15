package com.ringoid.origin.view.particles

import android.app.Activity
import android.view.ViewGroup
import android.view.animation.Interpolator
import com.github.jinatonic.confetti.ConfettiManager
import com.github.jinatonic.confetti.ConfettiManager.INFINITE_DURATION
import com.github.jinatonic.confetti.ConfettiSource
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

    fun init(activity: Activity) {
        context = activity
    }

    fun addGenerator(generator: ParticleGenerator) {
        generators[generator.id] = generator
    }

    fun animate(id: String, count: Int) {
        animateN(id, count)
    }

    private fun animateN(id: String, count: Int) {
        if (count <= 0 || !generators.containsKey(id)) {
            return
        }

        val duration = count / 2 * 1000L
        val generator = generators[id]!!
        val source = ConfettiSource(containerView.width / 3 - AppRes.ICON_SIZE_36 / 2, containerView.height - AppRes.MAIN_BOTTOM_BAR_HEIGHT)

        ConfettiManager(containerView.context, generator, source, containerView)
            .setEmissionDuration(duration)
            .setEmissionRate(2f)
            .setNumInitialCount(1)
            .setVelocityX(generator.velocityX, generator.velocityDevX)
            .setAccelerationX(generator.accelerationX, generator.accelerationDevX)
            .setTargetVelocityX(generator.targetVelocityX, generator.targetVelocityDevX)
            .setVelocityY(generator.velocityY, generator.velocityDevY)
            .setAccelerationY(generator.accelerationY, generator.accelerationDevY)
            .setTargetVelocityY(generator.targetVelocityY, generator.targetVelocityDevY)
            .setTTL(generator.ttl)
            .enableFadeOut(ParticleInterpolator(random.nextFloat() * 0.1f))
            .animate()
    }

    fun setContainerView(containerView: ViewGroup) {
        this.containerView = containerView
    }
}

internal class ParticleInterpolator(private val randomStart: Float) : Interpolator {

    override fun getInterpolation(input: Float): Float = maxOf(0f, 1 - randomStart - input)
}
