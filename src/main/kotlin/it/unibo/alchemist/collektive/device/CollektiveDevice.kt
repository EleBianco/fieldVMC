package it.unibo.alchemist.collektive.device

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.alchemist.device.sensors.DistanceSensor

/**
 * Representation of a Collektive device in Alchemist.
 * [P] is the position type, the [environment] property represent the environment in which the device is located,
 * the [node] property represent a node in the environment.
 */
class CollektiveDevice<P>(
    private val environment: Environment<Any?, P>,
    override val node: Node<Any?>,
) : NodeProperty<Any?>,
    EnvironmentVariables,
    DistanceSensor where P : Position<P> {
    /**
     * The ID of the node.
     */
    val id = node.id

    override fun <ID : Any> Aggregate<ID>.distances(): Field<ID, Double> {
        val nodePosition = environment.getPosition(node)
        return neighboring(nodePosition as Position<*>).map { neighborPosition ->
            @Suppress("UNCHECKED_CAST")
            nodePosition.distanceTo(neighborPosition.value as P)
        }
    }

    override fun cloneOnNewNode(node: Node<Any?>): NodeProperty<Any?> =
        CollektiveDevice(environment, node)

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(name: String): T = node.getConcentration(SimpleMolecule(name)) as T

    override fun <T> getOrNull(name: String): T? =
        if (isDefined(name)) {
            get(name)
        } else {
            null
        }

    override fun <T> getOrDefault(
        name: String,
        default: T,
    ): T = getOrNull(name) ?: default

    override fun isDefined(name: String): Boolean = node.contains(SimpleMolecule(name))

    override fun <T> set(
        name: String,
        value: T,
    ): T = value.also { node.setConcentration(SimpleMolecule(name), it) }

    companion object {
//        val kryo = Kryo()
//        init {
////            kryo.isRegistrationRequired = false
//            kryo.register(Euclidean2DPosition::class.java)
//            kryo.register(Euclidean2DPosition.javaClass)
//            kryo.register(DoubleArray::class.java)
//            kryo.register(Pair::class.java)
//            kryo.register(Candidacy::class.java)
//            kryo.register(Stability::class.java)
//        }
    }
}