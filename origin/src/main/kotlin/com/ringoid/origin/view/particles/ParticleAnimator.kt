package com.ringoid.origin.view.particles

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.view.ViewGroup
import android.view.animation.Interpolator
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.github.jinatonic.confetti.ConfettiManager
import com.github.jinatonic.confetti.ConfettiSource
import com.ringoid.origin.AppRes
import dagger.Reusable
import timber.log.Timber
import java.util.*
import javax.inject.Inject

const val PARTICLE_TYPE_LIKE = "particle_type_like"
const val PARTICLE_TYPE_MATCH = "particle_type_match"
const val PARTICLE_TYPE_MESSAGE = "particle_type_message"

@Reusable
class ParticleAnimator @Inject constructor() {

    @Inject lateinit var random: Random
    private lateinit var containerView: ViewGroup
    private val generators = mutableMapOf<String, ParticleGenerator>()

    fun addGeneratorForResource(id: String, context: Context, @DrawableRes resId: Int) {
        if (generators.containsKey(id)) {
            return
        }

        getBitmap(context, resId)?.let {
            generators[id] = BitmapParticleGenerator(it)
        }
    }

    fun animateOnce(id: String) {
        if (!generators.containsKey(id)) {
            return
        }

        val source = ConfettiSource(containerView.width / 3, containerView.height - AppRes.MAIN_BOTTOM_BAR_HEIGHT_HALF)

        ConfettiManager(containerView.context, generators[id], source, containerView)
            .setNumInitialCount(1)
            .setVelocityX(-25f, 75f)
            .setAccelerationX(-50f, 25f)
            .setTargetVelocityX(0f, 12.5f)
            .setVelocityY(-660f, 80f)
            .setTTL(3000L)
            .enableFadeOut(ParticleInterpolator(random.nextFloat() / 10f))
            .animate()
    }

    fun animate(id: String, count: Int) {
        if (count <= 0 || !generators.containsKey(id)) {
            return
        }

        (1..count).forEach { animateOnce(id) }
    }

    fun setContainerView(containerView: ViewGroup) {
        this.containerView = containerView
    }

    // --------------------------------------------------------------------------------------------
    private fun getBitmap(context: Context, @DrawableRes resId: Int): Bitmap? =
        ContextCompat.getDrawable(context, resId)?.let {
            when (it) {
                is BitmapDrawable -> it.bitmap
                is VectorDrawable -> getBitmap(it)
                else -> {
                    Timber.e("Unsupported drawable type: ${it.javaClass.simpleName}")
                    null
                }
            }
        }

    private fun getBitmap(drawable: VectorDrawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

internal class ParticleInterpolator(private val randomStart: Float) : Interpolator {

    override fun getInterpolation(input: Float): Float = maxOf(0f, 1 - randomStart - input)
}
