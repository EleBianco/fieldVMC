package it.unibo.common.cbf.impl

import it.unibo.common.cbf.SDF
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt

class Star (
    private val center: Pair<Double, Double>,
    private val radius: Double,
    private val n: Int,
    m: Double = n / 2.0 //m determines how much the angles between the points are profound
) : SDF {

    private val an = PI / n.toDouble()
    private val en = PI / m

    private val acsX = cos(an)
    private val acsY = sin(an)
    private val ecsX = cos(en)
    private val ecsY = sin(en)

    override fun invoke(p: Pair<Double, Double>): Double {
        var px = p.first - center.first
        var py = p.second - center.second

        val angle = atan2(px, py)
        val twoAn = 2.0 * an

        val bn = angle.mod(twoAn) - an

        val rPoint = sqrt(px * px + py * py)
        px = rPoint * cos(bn)
        py = rPoint * abs(sin(bn))

        px -= radius * acsX
        py -= radius * acsY

        val dot = px * ecsX + py * ecsY
        val maxClamp = radius * acsY / ecsY

        val clamped = (-dot).coerceIn(0.0, maxClamp)

        px += ecsX * clamped
        py += ecsY * clamped

        val distance = sqrt(px * px + py * py)
        return distance * sign(px)
    }
}