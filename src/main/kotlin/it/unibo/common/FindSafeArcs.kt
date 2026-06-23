package it.unibo.common

import kotlin.math.PI

fun findSafeArcs(r: Double, validator: (Double) -> Double): List<Angle>{
    val zeros = findZeros(r, validator).sorted() //dovrebbero già essere in ordine per come è fatto findZeros!
    //però sempre meglio dare una controllata?

    if (zeros.isEmpty()){
        // tutta safe o tutta unsafe
        return if (validator(0.0) >= 0) listOf(Angle(0.0, 2* PI)) else emptyList()
    }

    val fullCircle = zeros + (zeros.first() + 2 * PI)

    val safeArcs = fullCircle
                    .zipWithNext { a, b -> Angle(a, b - a) }
                    .filter { isArcSafe(it, validator) }

    // è possibile che ci siano due archi da unire? SI se sono stati trovati degli zeri di tangenza
    // Quello che va da zeros.last a zeros.first potrebbe dover essere unito al primo

    return mergeArcs(safeArcs)
}

/**
 * Useful to determinate if the [arc] describes a valid region.
 * Using the [validator] test if one point of the arc is valid or not
 * for properties of construction all the points in the arc have the same validity.
 * If the [validator] returns 0 iterates until finds a point in witch return non-zero or a max number of times
 * If all the points tested return 0 assumes it to be an arc sovrapposto al border so is valid
 */
fun isArcSafe(arc: Angle, validator: (Double) -> Double): Boolean {

    var divisor = 2.0
    val maxDepth = 10 // Limite di sicurezza per evitare loop infiniti
    var depth = 0

    while (depth < maxDepth) {
        val step = arc.arc / divisor

        // Controlliamo il punto a sinistra del centro
        var d = validator(arc.from + step)
        if (d > 0.0) return true
        if (d < 0.0) return false

        // Controlliamo il punto a destra del centro (con divisor = 2.0) controlliamo due volte lo stesso punto
        d = validator(arc.from + arc.arc - step)
        if (d > 0.0) return true
        if (d < 0.0) return false

        divisor *= 2.0
        depth++
    }

    // se tutti i test ci hanno dato esattamente 0 assumiamo che tutto l'arco sia sovrapposto al bordo
    //consideriamo il bordo ancora safe
    return true
}