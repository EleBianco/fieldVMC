package it.unibo.collektive.utils

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.common.calculateAngle
import it.unibo.common.findSafeArcs
import it.unibo.common.minus
import it.unibo.common.plus
import java.io.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

const val EPSILON = 1e-9

/**
 * Type alias for a function that checks if a position is in a safe space.
 */
typealias SafeSpaceChecker = (Pair<Double, Double>) -> Double

private fun pointsDistance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double =
    sqrt((p1.first - p2.first).pow(2) + (p1.second - p2.second).pow(2))

private fun arco(p: Pair<Double, Double>) : Double {
    val (x, y) = p
    val xCent = 2.0
    val yCent = 16.0
    val radius = 7.0
    val sAng = - PI / 2.0
    val eAng = PI
    val angle = atan2(y - yCent, x - xCent)
    val pS = Pair(xCent + radius * cos(sAng), yCent + radius * sin(sAng))
    val pE = Pair(xCent + radius * cos(eAng), yCent + radius * sin(eAng))

    val dist = if (angle in sAng..eAng) {
        radius - pointsDistance(p, Pair(xCent, yCent))
    } else {
        min(pointsDistance(p, pS), pointsDistance(p, pE))
    }

    return abs(dist)
}

private fun segmento(p: Pair<Double, Double>) : Double {
    val a = Pair(2.0 , 2.0)
    val b = Pair(2.0, 9.0)

    val abX = b.first - a.first
    val abY = b.second - a.second
    val apX = p.first - a.first
    val apY = p.second - a.second

    // Lunghezza al quadrato del segmento AB (|AB|^2)
    val abLenSq = abX * abX + abY * abY

    // Caso limite: Se A e B coincidono, il segmento è un punto.
    // La distanza è semplicemente quella tra P e A.
    if (abLenSq == 0.0) return pointsDistance(p, a)

    // Calcolo del fattore di proiezione t tramite prodotto scalare
    val t = (apX * abX + apY * abY) / abLenSq

    // Clamping di t nell'intervallo [0.0, 1.0] per non uscire dal segmento
    val tClamped = t.coerceIn(0.0, 1.0)

    // Coordinate del punto più vicino sul segmento
    val closestX = a.first + tClamped * abX
    val closestY = a.second + tClamped * abY

    // Restituisce la distanza euclidea tra P e il punto più vicino trovato
    return pointsDistance(p, Pair(closestX, closestY))
}

private fun defaultSafeSpaceChecker(p: Pair<Double, Double>): Double {


    return 100.0

}

/**
 * Type alias for a function that spawns devices in an aggregate given some parameters:
 * - [devSpawn] the device spawn sensor;
 * - [locationSensor] the location sensor;
 * - [potential] the potential of the node;
 * - [localSuccess] the local success of the node;
 * - [success] the global success of the node;
 * - [localResource] the local resources of the node.
 */
typealias Spawner = Aggregate<Int>.(
    devSpawn: DeviceSpawn,
    locationSensor: LocationSensor,
    potential: Double,
    localSuccess: Double,
    success: Double,
    localResource: Double,
) -> Unit

data class Stability(
    val spawnStable: Boolean = false,
    val destroyStable: Boolean = false,
): Serializable {
    infix fun and(other: Stability): Boolean = spawnStable && other.spawnStable && destroyStable && other.destroyStable
}

/**
 * The policies that determine if a node should be spawned or destroyed.
 * The node is spawned if the local resources are above the lower-bound threshold,
 * if it has less than a maximum threshold of children and the neighborhood is stable.
 * The node is destroyed if the local resources are below the lower bound,
 * if it is not father of any node and the neighborhood is stable.
 */
fun Aggregate<Int>.determineStability(
    childrenCount: Int,
    localResource: Double,
    lastChanged: Double,
    now: Double,
    potential: Double,
    localPosition: Pair<Double, Double>,
    neighborPositions: List<Pair<Double, Double>>,
    localStability: Stability,
    devSpawn: DeviceSpawn,
    env: EnvironmentVariables,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    safeSpaceChecker: SafeSpaceChecker = ::defaultSafeSpaceChecker,
): Stability {
    val enoughTime = now > lastChanged + devSpawn.minSpawnWait
    val everyoneIsDestroyStable = now > lastChanged
    val everyoneIsStable = localStability.spawnStable && localStability.destroyStable && enoughTime
    env["enough-time"] = enoughTime
    env["everyone-is-stable"] = everyoneIsStable
    env["everyone-is-destroy-stable"] = everyoneIsDestroyStable
    return when {
        potential > 0.0
            && childrenCount == 0
            && localResource < resourceS.resourceLowerBound
            && everyoneIsDestroyStable -> {
            devSpawn.selfDestroy()
            Stability(spawnStable = false, destroyStable = false)
        }
        neighborPositions.isEmpty() ||
            localResource / (2 + childrenCount) > resourceS.resourceLowerBound &&
            childrenCount < devSpawn.maxChildren &&
            everyoneIsStable -> {
            val safeArcs = findSafeArcs(devSpawn.cloningRange) { angle ->
                val x = devSpawn.cloningRange * cos(angle)
                val y = devSpawn.cloningRange * sin(angle)
                safeSpaceChecker(localPosition + (x to y))
            }
            val relativePositions = neighborPositions.map { it - localPosition }
            val angles = relativePositions.map { atan2(it.second, it.first) }.sorted() //in angles ci sono gli angoli a cui si trovano i vicini, non gli angoli validi
            val angle = calculateAngle(angles, random, devSpawn.maxChildren, safeArcs)
            when {
                angle.isNaN() -> Stability(spawnStable = true, destroyStable = true)
                else -> {
                    val x = devSpawn.cloningRange * cos(angle)
                    val y = devSpawn.cloningRange * sin(angle)
                    val absoluteDestination = localPosition + (x to y)

                    val control = safeSpaceChecker(absoluteDestination)
                    if(control < -EPSILON){
                        println("Spown non valido!"+ localId + control)
                    }

                    devSpawn.spawn(absoluteDestination)
                    Stability(spawnStable = false, destroyStable = localStability.destroyStable)
                }
            }
        }
        else -> Stability(spawnStable = enoughTime, destroyStable = now > lastChanged)
    }
}
