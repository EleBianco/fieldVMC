package it.unibo.common.cbf.impl

import it.unibo.common.cbf.SDF

class LetterE (
    start: Pair<Double, Double>,
    height: Double,
) : SDF{
    private val vertical = Segment(start, Pair(start.first, start.second + height))
    private val horizontalLow = Segment(start, Pair(start.first + height / 2.0, start.second))
    private val horizontalCenter = Segment(Pair(start.first, start.second + height / 2.0), Pair(start.first + height * 3.0 / 8.0, start.second + height / 2.0))
    private val horizontalHigh = Segment(Pair(start.first, start.second + height), Pair(start.first + height / 2.0, start.second + height))

    override fun invoke(p: Pair<Double, Double>): Double {
        return minOf(vertical(p), horizontalLow(p), horizontalCenter(p), horizontalHigh(p))
    }
}