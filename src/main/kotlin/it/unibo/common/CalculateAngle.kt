package it.unibo.common

import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import kotlin.math.PI

/**
 * Selects a [random] element from the iterable,
 * with a probability proportional to the value returned by the [by] function.
 */
fun <T> Iterable<T>.randomElementWeighted(
    random: RandomGenerator,
    by: T.() -> Double,
): T {
    val total = fold(0.0) { acc, element -> acc + by(element) }
    val selector: Double = random.nextRandomDouble(0.0..total)
    var accumulator = 0.0
    for (element in this) {
        accumulator += by(element)
        if (accumulator >= selector) {
            return element
        }
    }
    return last()
}

/**
 * Selects a random angle from the list of [angles],
 * with a probability proportional to the angle's distance from the previous and next angle.
 * The [maxChildren] parameter is used to calculate the minimum angle difference.
 */
/*fun calculateAngle(
    angles: List<Double>,
    random: RandomGenerator,
    maxChildren: Int,
    safeArcs: List<Angle>,
): Double {
    fun relativeAngleTowards(center: Double) = PI * random.nextGaussian() / maxChildren + center
    return when {
        angles.isEmpty() -> random.nextRandomDouble(0.0..2 * PI)
        angles.size == 1 -> relativeAngleTowards(angles.first() + PI)
        else -> {
            val fullCircle = angles + (angles.first() + 2 * PI)

            val minArc = 2 * PI / maxChildren
            val differences =
                fullCircle
                    .zipWithNext { a, b -> Angle(a, b - a) }
                    .filter { it.arc >= minArc }
            when {
                differences.isEmpty() -> Double.NaN
                else -> {
                    val selectedAngle = differences.randomElementWeighted(random) { arc }
                    relativeAngleTowards(selectedAngle.arc / 2 + selectedAngle.from)
                }
            }
        }
    }
}*/


fun calculateAngle(
    angles: List<Double>,
    random: RandomGenerator,
    maxChildren: Int,
    safeArcs: List<Angle>,
): Double {
    fun relativeAngleTowards(center: Double) = PI * random.nextGaussian() / maxChildren + center

    if (safeArcs.isEmpty()) return Double.NaN

    val minArc = 2 * PI / maxChildren

    val differences = when {
        angles.isEmpty() -> listOf(Angle(0.0, 2 * PI))
        angles.size == 1 -> listOf(Angle(angles.first(), 2 * PI))
        else -> {
            val sortedAngles = angles.sorted()
            val fullCircle = sortedAngles + (sortedAngles.first() + 2 * PI)
            fullCircle
                .zipWithNext { a, b -> Angle(a, b - a) }
                .filter { it.arc >= minArc }
        }
    }

    val linearDiffs = differences.flatMap { splitAtZero(it) }
    val linearSafe = safeArcs.flatMap { splitAtZero(it) } //questi dovrebbero essere già a posto

    val rawIntersections = mutableListOf<Angle>()
    for (diff in linearDiffs) {
        for (safe in linearSafe) {
            val start = maxOf(diff.from, safe.from)
            val end = minOf(diff.from + diff.arc, safe.from + safe.arc)

            if (start < end) { // Se c'è sovrapposizione
                rawIntersections.add(Angle(start, end - start))
            }
        }
    }

    /* NON POSSIAMO FARE MERGE PERCHè SE NO CI PERDIAMO I DOVE STAVANO I NEIGHBORS
    val mergedIntersections = mergeArcs(sortedRaw)*/

    //quindi come capiamo se l'arco a cavallo dello 0 va unito o no?
    val intersection = rawIntersections.sortedBy { it.from }


    val validIntersections = if( !angles.isEmpty() && angles.minOf { it } != 0.0) wrapArcs(intersection) else intersection

    return when {
        validIntersections.isEmpty() -> Double.NaN
        else -> {
            val selected = validIntersections.randomElementWeighted(random) { arc }
            relativeAngleTowards(selected.arc / 2 + selected.from)
                .coerceIn(selected.from, selected.from + selected.arc)

        }
    }
}

/**
 * Given [arc] it normalizes it in the range [0. 2PI)
 * If the arc passes the 0, it is split in two normalized arcs
 */
fun splitAtZero(arc: Angle): List<Angle> {

    val start = ((arc.from % (2 * PI)) + 2 * PI) % (2 * PI) // normalizza sia i positivi che i negativi
    val end = start + arc.arc

    return if (end > 2 * PI) {
        listOf(
            Angle(start, 2 * PI - start),
            Angle(0.0, end - 2 * PI)
        )
    } else {
        // l'arco non scavalla
        listOf(Angle(start, arc.arc))
    }
}