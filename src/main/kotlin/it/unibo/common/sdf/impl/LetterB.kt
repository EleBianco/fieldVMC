package it.unibo.common.sdf.impl

import it.unibo.common.sdf.SDF
import kotlin.math.PI

/**
 * Represents a 2D Signed Distance Field (SDF) of the letter B.
 *
 * @param start The (X, Y) coordinates of the starting point of the vertical stem.
 * @param height The total height of the letter.
 * @property thickness The thickness of the letter's strokes (default is 0.0).
 */
class LetterB (
    start: Pair<Double, Double>,
    height: Double,
    private val thickness: Double = 0.0,
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
        return minOf(vertical(p), highArc(p), lowArc(p), lowSeg(p), midSeg(p), highSeg(p)) - thickness
    }
}
