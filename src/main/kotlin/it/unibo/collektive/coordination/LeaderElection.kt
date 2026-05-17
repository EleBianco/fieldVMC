package it.unibo.collektive.coordination

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.alchemist.device.sensors.DistanceSensor
import it.unibo.collektive.stdlib.collapse.fold
import kotlinx.serialization.Serializable
/**
 * Elect the leader in an area limited by the [radius], based on the [localStrength] of the node.
 */
inline fun <reified ID : Any, reified C : Comparable<C>> Aggregate<ID>.boundedElection(
    distanceSensor: DistanceSensor,
    localStrength: C,
    radius: Double,
): ID {

    val local: Candidacy<ID, C> = Candidacy(localStrength, 0.0, localId)
    return share(local) { candidates ->
        val candidate =
            with(distanceSensor) {
                candidates.alignedMap(distances()) { _, c, m -> Candidacy(c.strength,
                    c.distance + m, c.leaderId) }
            }
        candidate.neighbors
            .fold(local) { accumulator, neighbor ->
                val newValue = neighbor.value
                val id = neighbor.id
                when {
                    (newValue.distance > radius || id == localId) -> accumulator
                    else -> minOf(accumulator, newValue)
            }
        }
    }.leaderId
}

@Serializable
data class Candidacy<ID : Any, C: Comparable<C>>(
    val strength: C,
    val distance: Double,
    val leaderId: ID,
) : Comparable<Candidacy<ID, C>> {
    override fun compareTo(other: Candidacy<ID, C>): Int =
        Comparator<Candidacy<ID, C>> { a, b -> b.strength.compareTo(a.strength) }
            .thenBy { it.distance }
            .thenBy {
                when (it.leaderId) {
                    is Comparable<*> -> it.leaderId
                    else -> 0
                }
            }.compare(this, other)
}
