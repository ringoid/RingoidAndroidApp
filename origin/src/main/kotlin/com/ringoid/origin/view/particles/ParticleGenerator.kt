package com.ringoid.origin.view.particles

import android.app.Activity
import android.graphics.Bitmap
import com.github.jinatonic.confetti.ConfettoGenerator
import com.github.jinatonic.confetti.confetto.BitmapConfetto
import com.github.jinatonic.confetti.confetto.Confetto
import com.ringoid.origin.R
import com.ringoid.utility.getScreenHeight
import com.ringoid.utility.getScreenWidth
import com.ringoid.utility.image.getBitmap
import java.util.*

abstract class ParticleGenerator(internal val id: String, activity: Activity) : ConfettoGenerator {

    protected val h = activity.getScreenHeight()
    protected val w = activity.getScreenWidth()

    internal open val accelerationX = 0f
    internal open val accelerationDevX = 0f
    internal open val velocityX = 0f
    internal open val velocityDevX = 0f
    internal open val targetVelocityX = 0f
    internal open val targetVelocityDevX = 0f

    internal open val accelerationY = 0f
    internal open val accelerationDevY = 0f
    internal open val velocityY = 0f
    internal open val velocityDevY = 0f
    internal open val targetVelocityY = 0f
    internal open val targetVelocityDevY = 0f

    internal open val ttl = 3500L
}

sealed class BitmapParticleGenerator(id: String, activity: Activity, private val bitmap: Bitmap)
    : ParticleGenerator(id, activity) {

    override fun generateConfetto(random: Random?): Confetto = BitmapConfetto(bitmap)
}

class LikesParticleGenerator(activity: Activity)
    : BitmapParticleGenerator(PARTICLE_TYPE_LIKE, activity, getBitmap(activity, R.drawable.ic_particle_like)!!) {

    override val accelerationX = w / 10f
    override val accelerationDevX = accelerationX * 0.1f
    override val velocityX = -50f
    override val velocityDevX = -velocityX * 3f
    override val targetVelocityX = 50f
    override val targetVelocityDevX = targetVelocityX * 3f

    override val accelerationY = h / 15f
    override val accelerationDevY = accelerationY * 0.1f
    override val velocityY = -h / 3.3f
    override val velocityDevY = velocityY * 0.5f
    override val targetVelocityY = -h / 4.5f
    override val targetVelocityDevY = targetVelocityY * 0.5f
}

class MatchesParticleGenerator(activity: Activity)
    : BitmapParticleGenerator(PARTICLE_TYPE_MATCH, activity, getBitmap(activity, R.drawable.ic_particle_match)!!) {

    override val accelerationX = w / 10f
    override val accelerationDevX = accelerationX * 0.1f
    override val velocityX = -30f
    override val velocityDevX = -velocityX * 3f
    override val targetVelocityX = 125f
    override val targetVelocityDevX = targetVelocityX * 3f

    override val accelerationY = h / 15f
    override val accelerationDevY = accelerationY * 0.1f
    override val velocityY = -h / 3.3f
    override val velocityDevY = velocityY * 0.5f
    override val targetVelocityY = -h / 4.5f
    override val targetVelocityDevY = targetVelocityY * 0.5f
}

class MessagesParticleGenerator(activity: Activity)
    : BitmapParticleGenerator(PARTICLE_TYPE_MESSAGE, activity, getBitmap(activity, R.drawable.ic_particle_message)!!) {

    override val accelerationX = w / 10f
    override val accelerationDevX = accelerationX * 0.1f
    override val velocityX = -10f
    override val velocityDevX = -velocityX * 3f
    override val targetVelocityX = 75f
    override val targetVelocityDevX = targetVelocityX * 3f

    override val accelerationY = h / 15f
    override val accelerationDevY = accelerationY * 0.1f
    override val velocityY = -h / 3.3f
    override val velocityDevY = velocityY * 0.5f
    override val targetVelocityY = -h / 3.3f
    override val targetVelocityDevY = targetVelocityY * 0.5f
}
