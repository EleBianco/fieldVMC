package it.unibo.common.cbf.impl

import it.unibo.common.cbf.SDF
import it.unibo.common.pointsDistance

class Segment (
    private val a: Pair<Double, Double>,
    private val b: Pair<Double, Double>
) : SDF {
    override fun invoke(p: Pair<Double, Double>): Double {

        val abX = b.first - a.first
        val abY = b.second - a.second
        val apX = p.first - a.first
        val apY = p.second - a.second

        val abLenSq = abX * abX + abY * abY

        if (abLenSq == 0.0) return pointsDistance(p, a)

        val t = (apX * abX + apY * abY) / abLenSq

        val tClamped = t.coerceIn(0.0, 1.0)

        val closestX = a.first + tClamped * abX
        val closestY = a.second + tClamped * abY

        return pointsDistance(p, Pair(closestX, closestY))
    }
}