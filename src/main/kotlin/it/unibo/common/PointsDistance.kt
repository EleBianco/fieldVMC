package it.unibo.common

import kotlin.math.pow
import kotlin.math.sqrt

fun pointsDistance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double =
    sqrt((p1.first - p2.first).pow(2) + (p1.second - p2.second).pow(2))