package com.ringoid.origin.view.particles

import android.graphics.Bitmap
import com.github.jinatonic.confetti.ConfettoGenerator
import com.github.jinatonic.confetti.confetto.BitmapConfetto
import com.github.jinatonic.confetti.confetto.Confetto
import java.util.*

abstract class ParticleGenerator(protected val bitmap: Bitmap) : ConfettoGenerator

class BitmapParticleGenerator(bitmap: Bitmap) : ParticleGenerator(bitmap) {

    override fun generateConfetto(random: Random?): Confetto = BitmapConfetto(bitmap)
}
