package it.unibo.common.cbf

fun interface SDF {
    operator fun invoke(p: Pair<Double, Double>): Double
}