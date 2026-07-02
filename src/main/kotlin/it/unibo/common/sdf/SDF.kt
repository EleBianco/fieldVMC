package it.unibo.common.sdf

fun interface SDF {
    /**
     * returns the distance of the point [p] from the surface of the SDF.
     * if the value is negative, the point is inside the SDF.
     */
    operator fun invoke(p: Pair<Double, Double>): Double
}

/**
 * Creates a new Signed Distance Field (SDF) representing the inverse of the given [shape].
 * * By negating the distance value, the internal regions (traditionally negative) become
 * external (positive), and the external regions become internal.
 */
fun inverseSDF(shape: SDF): SDF = SDF { p -> -shape(p) }
