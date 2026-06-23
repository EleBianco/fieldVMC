package it.unibo.common.cbf.impl

import it.unibo.common.cbf.SDF
import kotlin.math.PI
import kotlin.math.min

class Interrogative (
    center: Pair<Double, Double>,
    radius: Double,
) : SDF {

    private val arc = Arc(
        center,
        radius,
        - PI / 2.0,
        11.0 / 6.0 * PI
    )
    private val segment = Segment(
        Pair(center.first, center.second - radius),
        Pair(center.first, center.second - 2.0 * radius)
    )

    override fun invoke(p: Pair<Double, Double>): Double {
        return min(arc(p), segment(p))
    }
}