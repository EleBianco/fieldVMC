package it.unibo.collektive.vmc

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.DeviceSpawn
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.alchemist.device.sensors.LocationSensor
import it.unibo.collektive.alchemist.device.sensors.RandomGenerator
import it.unibo.collektive.alchemist.device.sensors.ResourceSensor
import it.unibo.collektive.alchemist.device.sensors.SuccessSensor
import it.unibo.collektive.alchemist.device.sensors.DistanceSensor
import it.unibo.collektive.coordination.findParent
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.lib.convergeSuccess
import it.unibo.collektive.lib.findPotential
import it.unibo.collektive.lib.obtainLocalSuccess
import it.unibo.collektive.lib.spreadResource
import it.unibo.collektive.stdlib.collapse.countMatching
import it.unibo.collektive.utils.SpawnerNoStability
import it.unibo.collektive.utils.determineSpawn

/**
 * Entrypoint of the VMC algorithm, using spawning and destroying after stability policies.
 */
fun Aggregate<Int>.fixedRootWithSpawning(
    devSpawn: DeviceSpawn,
    distanceS: DistanceSensor,
    env: EnvironmentVariables,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
): Double = fixedRootStability(devSpawn, distanceS, env, locationS, random, resourceS, successS)

fun Aggregate<Int>.fixedRootStability(
    devSpawn: DeviceSpawn,
    distanceS: DistanceSensor,
    env: EnvironmentVariables,
    locationS: LocationSensor,
    random: RandomGenerator,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
): Double =
    with(distanceS) {
        vmcFixedLeader(
            devSpawn,
            distanceS,
            env,
            locationS,
            resourceS,
            successS,
        ) { devSpawn, locationS, potential: Double, localSuccess: Double, success: Double, localResource: Double ->
            val children = neighboring(findParent(potential))
            env["children-around"] = children
            env["parent"] = children.local.value
            val childrenCount = children.neighbors.countMatching { it.value == localId }
            env["children-count"] = childrenCount
            val neighbors = neighboring(locationS.coordinates())
            val localPosition = neighbors.local.value
            val neighborPositions = locationS.surroundings()
            determineSpawn(childrenCount, localResource, localPosition, neighborPositions, devSpawn, random, resourceS)
        }
    }

fun Aggregate<Int>.vmcFixedLeader(
    devSpawn: DeviceSpawn,
    distanceS: DistanceSensor,
    env: EnvironmentVariables,
    locationS: LocationSensor,
    resourceS: ResourceSensor,
    successS: SuccessSensor,
    spawner: SpawnerNoStability,
): Double {
    val isLeader = env.get<Boolean>("leader")
    val potential = findPotential(distanceS, isLeader)
    val localSuccess = obtainLocalSuccess(successS)
    val success = convergeSuccess(successS, potential, localSuccess)
    val localResource =
        spreadResource(env, potential, success, resourceS.maxResource)
            .also { env["local-resource"] = it }
    spawner(devSpawn, locationS, potential, localSuccess, success, localResource)
    return localResource
}
