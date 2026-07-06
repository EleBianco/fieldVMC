package it.unibo.common.sdf.impl

import it.unibo.common.sdf.SDF

/**
 * Represents a 2D Signed Distance Field (SDF) of the letter E.
 *
 * @param start The (X, Y) coordinates of the starting point (bottom-left) of the vertical stem.
 * @param height The total height of the letter.
 * @property thickness The thickness of the letter's strokes (default is 0.0).
 */
class LetterE (
    start: Pair<Double, Double>,
    height: Double,
    private val thickness: Double = 0.0,
) : SDF{
    private val vertical = Segment(start, Pair(start.first, start.second + height))
    private val horizontalLow = Segment(start, Pair(start.first + height / 2.0, start.second))
    private val horizontalCenter = Segment(Pair(start.first, start.second + height / 2.0), Pair(start.first + height * 3.0 / 8.0, start.second + height / 2.0))
    private val horizontalHigh = Segment(Pair(start.first, start.second + height), Pair(start.first + height / 2.0, start.second + height))

    override fun invoke(p: Pair<Double, Double>): Double {
        return minOf(vertical(p), horizontalLow(p), horizontalCenter(p), horizontalHigh(p)) - thickness
    }
}
