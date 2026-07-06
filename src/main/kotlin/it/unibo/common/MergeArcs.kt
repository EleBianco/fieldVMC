package it.unibo.common

import kotlin.math.PI
import kotlin.math.abs

/**
 * A small constant used as a tolerance threshold for floating-point comparisons.
 */
const val EPSILON = 1e-8

/**
 * Given an ordered list [arcs]
 * return a list where al the arcs that were contiguous are united in one single arc.
 * */
fun mergeArcs(arcs: List<Angle>): List<Angle> {
    if (arcs.size <= 1) return arcs

    val merged = mutableListOf<Angle>()
    var current = arcs.first()

    for (i in 1 until arcs.size) {
        val next = arcs[i]
        // Se il prossimo inizia esattamente dove finisce il corrente (con tolleranza EPSILON)
        if (abs(current.from + current.arc - next.from) < EPSILON) {
            //non lo aggiungo ancora perché potrebbe essere da fare un accorpamento di più archi
            current = Angle(current.from, current.arc + next.arc)
        } else {
            //se non ho fatto merge vuol dire che quello che stavo controllando è finito
            merged.add(current)
            current = next
        }
    }
    merged.add(current) // Aggiungiamo l'ultimo

    return merged
}

/**
 * Given an ordered list [arcs] check if the last arc is contiguous to the first one,
 * if so, merge them.
 */
fun wrapArcs(arcs: List<Angle>): List<Angle> {
    if (arcs.size <= 1) return arcs

    val first = arcs.first()
    val last = arcs.last()

    // Se l'ultimo arco finisce esattamente (con tolleranza) dove inizia il primo + un giro
    return if(abs(last.from + last.arc - (first.from + 2 * PI)) < EPSILON) {
        val combinedArc = last.arc + first.arc

        val merged = Angle(last.from, combinedArc)

        // Prendiamo tutti gli archi centrali, scartando il primo (drop(1)) e l'ultimo (dropLast(1))
        val middleArcs = arcs.drop(1).dropLast(1)

        middleArcs + merged
    }
    else arcs
}
