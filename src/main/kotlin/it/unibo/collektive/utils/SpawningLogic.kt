package it.unibo.collektive.utils

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.common.calculateAngle
import it.unibo.common.cbf.impl.Arc
import it.unibo.common.findSafeArcs
import it.unibo.common.minus
import it.unibo.common.plus
import java.io.Serializable
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

const val EPSILON = 1e-9

/**
 * Type alias for a function that checks if a position is in a safe space.
 */
typealias SafeSpaceChecker = (Pair<Double, Double>) -> Double

private fun defaultSafeSpaceChecker(p: Pair<Double, Double>): Double {

    val sdf = Arc(Pair(0.0, 10.0), 10.0, PI / 2.0, 5.0 /4.0 * PI)
    val thickness = 2.5

    return thickness - sdf(p)
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
