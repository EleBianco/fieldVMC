package it.unibo.common

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sign

//valori indicativi da capire come tararli bene!
const val MIN = 1e-7
const val TOLERANCE = 1e-10
const val IMPRECISION = 0.95

/**
 * Given a distance [r] (the distance of the spawn for example)
 * and a [validator] that taken an angle returns the distance from the nearest border
 * returns a collection of all the angles in which occurs an intersection between the circumference and the border.
 *
 * N.B.: non trova punti di tangenza a meno che non ci caschi esattamente sopra perché non c'è cambio di segno.
 *      E non ci casca sopra praticamente mai perché ci spostiamo sempre di un po' meno di [d] per correggere le approssimazioni!
 * N.M.B.: se due zeri hanno una distanza tra loro minore di MIN potremmo saltarli e non trovare nessuno dei due
 * */
fun findZeros(r: Double, validator: (Double) -> Double): List<Double> {
    
    val zeros = mutableListOf<Double>()
    var angle = 0.0
    var prev = 0.0
    var prevd = 0.0


    while (angle < 2 * PI) {
        var d = validator(angle)  * IMPRECISION

        if (prevd != 0.0 && d!= 0.0 && sign(d) != sign(prevd)) {
            zeros.add(bisection(angle, prev, validator))
        }

        prev = angle
        prevd = d
        d = abs(d)

        if (d > 2 * r) {
            return zeros //che dovrebbe sempre essere empty se SDF e calcoli sono definiti bene!

        } else {

            if (d == 0.0) { //vogliamo 0 o ci basta che sia < di un tot?
                zeros.add(angle)
            }

            //ci spostiamo esattamente di delta?
            //con approssimazioni di calcolo o SDF non del tutto precisi (smooth min) d potrebbe non essere perfetta
            // => ora ho messo IMPRECISION e ci spostiamo proporzionale a MIN invece che di un valore fisso

            val delta = if( d < MIN ) 2 * asin(MIN / (2 * r)) else 2 * asin((d) / (2 * r))
            angle += delta
        }

    }

    //serve un controllo di wrap around?
    //in teoria no perchè se anche con d ridotta abbiamo superato 2PI significa che lo
    // zero era oltre 0.0 e lo abbiamo già trovato?!
    // e se ci siamo mossi di min?

    //wrap around (per ora per sicurezza)
    val dFirst = validator(0.0)
    if (prevd != 0.0 && dFirst != 0.0 && sign(dFirst) != sign(prevd)) {
        zeros.add(bisection(2 * PI, prev, validator))
    }

    return zeros
}

fun bisection(first: Double, second: Double, validator: (Double) -> Double): Double {

    var pos: Double
    var neg: Double

    if( validator(first) < 0.0 && validator(second) > 0.0 ) {
        neg = first
        pos = second
    } else if(validator(first) > 0.0 && validator(second) < 0.0) {
        neg = second
        pos = first
    } else {
        throw IllegalArgumentException("bisection invalid")
    }

    var temp: Double

    do {
        temp = (pos + neg) / 2.0

        val d = validator(temp)

        if( d < 0.0) {
            neg = temp
        } else if( d > 0.0) {
            pos = temp
        } else {
            return temp
        }

    } while (abs(pos - neg) > TOLERANCE)

    return temp

}