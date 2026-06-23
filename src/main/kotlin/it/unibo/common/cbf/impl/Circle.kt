package it.unibo.common.cbf.impl

import it.unibo.common.cbf.SDF
import it.unibo.common.pointsDistance

class Circle (
    private val center: Pair<Double, Double>,
    private val radius: Double,
) : SDF {
    override fun invoke(p: Pair<Double, Double>): Double {
        return radius - pointsDistance(p, center)
    }
}