package it.unibo.common

data class Angle(
    val from: Double,
    val arc: Double,
) : Comparable<Angle> {
    override fun compareTo(other: Angle): Int = compareBy(Angle::arc).thenBy(Angle::from).compare(this, other)
}