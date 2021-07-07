import three.js.Object3D
import three.js.Vector3
import kotlin.math.*

fun r(a: Double, e: Double, θ: Double) = a * (1 - e.pow(2)) / (1 + e * cos(θ))
fun cosϴ(a: Double, e: Double, r: Double) = a * (1.0 - e.pow(2)) / r / e - 1.0 / e
fun cosE(e: Double, cosϴ: Double) = (e + cosϴ) / (1.0 + e * cosϴ)
fun trueAnomaly(a: Double, e: Double, r: Double): Double {
    val cosTheta = cosϴ(a, e, r)
    console.log(" r $r cosϴ $cosTheta")
    return acos(cosTheta)
}
fun flightPathAngle(e: Double, θ: Double) = atan(e * sin(θ) / (1 + e * cos(θ)))

class OrbitParams(
    val name: String,
    val body: Object3D,
    val orbited: Object3D,
    val massOrbited: Double,
    val semiMajorAxis: Double,
    val eccentricity: Double,
    val inclination: Double
) {
    val r: Number get() {
        orbited.updateWorldMatrix(true, true)
        val bodyPosition = Vector3().apply(body::getWorldPosition)
        val orbitingPosition = Vector3().apply(orbited::getWorldPosition)
        val distance = bodyPosition.distanceTo(orbitingPosition)
        console.log("$name ${JSON.stringify(bodyPosition)} focus ${JSON.stringify(orbitingPosition)} d $distance")
        return distance
    }

    val μ get() = G * massOrbited

    fun v(r: Double) = sqrt(2 * μ / r / 1000.0 - μ / semiMajorAxis / 1000.0) / 1000.0

//    val cosE get() = cosE(eccentricity, cosϴ(semiMajorAxis, eccentricity, r.toDouble()))

    fun trueAnomaly(r: Double) = trueAnomaly(semiMajorAxis, eccentricity, r).let {
        if (body.position.y > 0.0) it
        else 2 * PI - it
    }

    fun flightPathAngle(trueAnomaly: Double) = flightPathAngle(eccentricity, trueAnomaly)

    fun vVector(r: Double): Vector3 {
        val v = v(r)
        val trueAnomaly = trueAnomaly(r)
        val flightPathAngle = flightPathAngle(trueAnomaly)
        println("r $r v $v theta ${trueAnomaly * 180 / PI} gamma ${flightPathAngle * 180 / PI}")
        return Vector3(v * sin(trueAnomaly + flightPathAngle), 0.0, v * cos(trueAnomaly + flightPathAngle))
    }

    fun deltaPosition(tau: Double, elapsedTime: Double) {
        val E = keplerSolve(eccentricity, M(meanMotion(G * massOrbited, semiMajorAxis * 1000), tau, elapsedTime))
        val cosE = cos(E)
        val cosTheta = cosTheta(eccentricity, cosE)
        val r = r(semiMajorAxis, eccentricity, acos(cosTheta))
        val x = cosTheta * r
        val y = -sinTheta(eccentricity, cosE, sin(E)) * r
        console.log("t $elapsedTime r $r x $x y $y")
        body.position.set(x, 0.0, y)
        body.updateMatrix()
        console.log("position ${JSON.stringify(body.position)}")
    }
}