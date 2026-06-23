package it.unibo.common.cbf.impl

import it.unibo.common.cbf.SDF
import kotlin.math.PI

class LetterB (
    start: Pair<Double, Double>,
    height: Double,
) : SDF {

    private val vertical = Segment(start, Pair(start.first, start.second + height))
    private val lowArc = Arc(
        Pair(start.first + height / 4.0, start.second + height / 4.0),
        height / 4.0,
        -PI / 2.0,
        PI,
    )
    private val highArc = Arc(
        Pair(start.first + height / 8.0, start.second + height * 3.0 / 4.0),
        height / 4.0,
        -PI / 2.0,
        PI,
    )
    private val lowSeg = Segment(start, Pair(start.first  + height / 4.0, start.second))
    private val midSeg = Segment(Pair(start.first, start.second + height / 2.0), Pair(start.first  + height / 4.0, start.second + height / 2.0))
    private val highSeg = Segment(Pair(start.first, start.second + height), Pair(start.first  + height / 8.0, start.second + height))

    override fun invoke(p: Pair<Double, Double>): Double {
        return minOf(vertical(p), highArc(p), lowArc(p), lowSeg(p), midSeg(p), highSeg(p))
    }
}