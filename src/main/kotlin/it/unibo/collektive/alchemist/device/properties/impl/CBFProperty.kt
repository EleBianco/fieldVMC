package it.unibo.collektive.alchemist.device.properties.impl

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.collektive.alchemist.device.properties.CBF
import it.unibo.common.sdf.SDF

class CBFProperty<T> (
    override val node: Node<T>,
    private val sdf: SDF
) : CBF, NodeProperty<T> {

    override fun isSafe(p: Pair<Double, Double>): Double =
        sdf(p)

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> =
        CBFProperty(node, sdf)
}