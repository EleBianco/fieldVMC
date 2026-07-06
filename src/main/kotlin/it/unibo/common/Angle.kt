package it.unibo.common

/**
 * Represents an angular sector defined by a starting angle and an arc length.
 *
 * @property from The starting angle of the sector.
 * @property arc The span or length of the angular arc.
 */
data class Angle(
    val from: Double,
    val arc: Double,
) : Comparable<Angle> {
    /**
     * Compares this angle with another based on the arc length first, then the starting angle.
     */
    override fun compareTo(other: Angle): Int = compareBy(Angle::arc).thenBy(Angle::from).compare(this, other)
}
