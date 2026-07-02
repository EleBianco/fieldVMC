package it.unibo.collektive.alchemist.device.properties

interface CBF {
    /**
     * Returns a value greater or equal to 0 if the point is safe, otherwise returns a negative value.
     */
    fun isSafe(p: Pair<Double, Double>) : Double
}