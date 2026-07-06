package it.unibo.common

import kotlin.math.PI

/**
 * Identifies and returns a list of arcs that lie entirely within a safe region.
 *
 * The safe region is determined along a circumference of radius [r] using the provided [validator]
 * function, which computes the distance from a specific point (given its angle) to the boundary.
 */
fun findSafeArcs(r: Double, validator: (Double) -> Double): List<Angle>{
    val zeros = findZeros(r, validator).sorted() //dovrebbero già essere in ordine per come è fatto findZeros!
    //però sempre meglio dare una controllata?

    if (zeros.isEmpty()){
        // tutta safe o tutta unsafe
        return if (validator(0.0) >= 0) listOf(Angle(0.0, 2* PI)) else emptyList()
    }

    val fullCircle = zeros + (zeros.first() + 2 * PI)

    val safeArcs = fullCircle
                    .zipWithNext { a, b -> Angle(a, b - a) }
                    .filter { isArcSafe(it, validator) }

    // è possibile che ci siano due archi da unire? SI se sono stati trovati degli zeri di tangenza
    // Quello che va da zeros.last a zeros.first potrebbe dover essere unito al primo

    return mergeArcs(safeArcs)
}

/**
 * Determines whether a given [arc] describes a valid (safe) region.
 *
 * The function evaluates the arc's safety by sampling points using the provided [validator].
 * Due to the construction properties, the entire arc shares the same validity as any of its
 * sampled points that do not evaluate to zero.
 *
 * If the [validator] evaluates to `0.0` (indicating the point lies exactly on a boundary),
 * the algorithm iteratively samples additional points at increasing depths up to a maximum limit.
 * If all tested points evaluate to `0.0`, the arc is assumed to perfectly overlap the boundary
 * and is considered safe.
 */
fun isArcSafe(arc: Angle, validator: (Double) -> Double): Boolean {
    val maxDepth = 10
    var divisor = 2.0
    var isSafe = true

    for (depth in 0 until maxDepth) {
        val step = arc.arc / divisor

        val dLeft = validator(arc.from + step)
        if (dLeft != 0.0) {
            isSafe = dLeft > 0.0
            break
        }

        val dRight = validator(arc.from + arc.arc - step)
        if (dRight != 0.0) {
            isSafe = dRight > 0.0
            break
        }

        divisor *= 2.0
    }

    return isSafe
}
