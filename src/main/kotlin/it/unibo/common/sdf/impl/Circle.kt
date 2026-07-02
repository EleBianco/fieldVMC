package it.unibo.common.sdf.impl

import it.unibo.common.sdf.SDF
import it.unibo.common.pointsDistance
import kotlin.math.abs

class Circle (
    private val center: Pair<Double, Double>,
    private val radius: Double,
    private val isRing: Boolean = false,
    private val thickness: Double = 0.0,
) : SDF {
    override fun invoke(p: Pair<Double, Double>): Double {
        val circleDist = radius - pointsDistance(p, center)

        return if (isRing) abs(circleDist) - thickness else circleDist
    }
}