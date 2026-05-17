package it.unibo.alchemist.collektive.device

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Output
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.alchemist.device.sensors.DistanceSensor
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.collektive.networking.InMemoryMessage
import it.unibo.collektive.networking.Mailbox
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.NeighborsData
import it.unibo.collektive.networking.NoNeighborsData
import it.unibo.collektive.networking.OutboundEnvelope

/**
 * Representation of a Collektive device in Alchemist.
 * [P] is the position type, the [environment] property represent the environment in which the device is located,
 * the [node] property represent a node in the environment.
 */
class CollektiveDevice<P>(
    private val environment: Environment<Any?, P>,
    override val node: Node<Any?>,
    private val retainMessagesFor: Time? = null,
    private val randomGenerator: RandomGenerator? = null,
) : NodeProperty<Any?>,
    EnvironmentVariables,
    Mailbox<Any>,
    DistanceSensor where P : Position<P> {

    constructor(
        randomGenerator: RandomGenerator,
        environment: Environment<Any?, P>,
        node: Node<Any?>,
        retainMessagesFor: Time?,
    ) : this(
        environment = environment,
        node = node,
        retainMessagesFor = retainMessagesFor,
        randomGenerator = null,
    )

    private data class TimedMessage(
        val receivedAt: Time,
        val payload: InMemoryMessage<Int>,
    )

    /**
     * The current time.
     */
    var currentTime: Time = Time.ZERO

    override val inMemory = false

    /**
     * The ID of the node.
     */
    val id = node.id

    private val validMessages: MutableList<TimedMessage> = mutableListOf()

    private fun receiveMessage(
        time: Time,
        message: InMemoryMessage<Int>,
    ) {
        validMessages += TimedMessage(time, message)
    }

    override fun <ID : Any> Aggregate<ID>.distances(): Field<ID, Double>{
        val nodePosition = environment.getPosition(node)
        return neighboring(nodePosition as Position<P>).map { position ->
            @Suppress("UNCHECKED_CAST")
            nodePosition.distanceTo(position.value as P) }
    }

    override fun cloneOnNewNode(node: Node<Any?>): NodeProperty<Any?> =
        CollektiveDevice(environment, node, retainMessagesFor)

    


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

    override fun currentInbound(): NeighborsData<Any> {
        return NoNeighborsData<Any>()
    }

    override fun deliverableFor(outboundMessage: OutboundEnvelope<Any>) {
        TODO("Not yet implemented")
    }

    override fun deliverableReceived(message: Message<Any, *>) {
        TODO("Not yet implemented")
    }

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
