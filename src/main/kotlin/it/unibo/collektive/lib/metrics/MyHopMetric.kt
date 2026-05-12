package it.unibo.collektive.lib.metrics

import it.unibo.collektive.alchemist.device.sensors.DistanceSensor
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.neighboring

data class MyHopMetric(
    val step: Double = 1.0,
) : DistanceSensor {
    override fun <ID : Any> Aggregate<ID>.distances(): Field<ID, Double> =
        neighboring(step)
            .map {
                if (it.id == localId) 0.0 else it.value + step
            }
}
