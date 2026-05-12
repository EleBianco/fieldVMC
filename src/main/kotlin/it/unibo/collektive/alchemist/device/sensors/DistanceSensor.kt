package it.unibo.collektive.alchemist.device.sensors

import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate

/**
 * Provides distance information between aggregate devices.
 */
interface DistanceSensor {
    /**
     * Computes the distance from the local device to each aligned device in the aggregate field.
     */
    fun <ID : Any> Aggregate<ID>.distances(): Field<ID, Double>
}