import three.js.*
import kotlin.math.*

fun r(a: Double, e: Double, θ: Double) = a * (1 - e.pow(2)) / (1 + e * cos(θ))
fun cosϴ(a: Double, e: Double, r: Double) = a * (1.0 - e.pow(2)) / r / e - 1.0 / e
fun cosE(e: Double, cosϴ: Double) = (e + cosϴ) / (1.0 + e * cosϴ)
fun trueAnomaly(a: Double, e: Double, r: Double): Double {
  val cosTheta = cosϴ(a, e, r)
//    console.log(" r $r cosϴ $cosTheta")
  return acos(cosTheta)
}
fun flightPathAngle(e: Double, θ: Double) = atan(e * sin(θ) / (1 + e * cos(θ)))

fun makeOrbitLine(semiMajorAxis: Double, eccentricity: Double, segments: Int = 1000) = EllipseCurve<Vector3>(
  xRadius = semiMajorAxis,
  yRadius = semiMinorAxis(eccentricity, semiMajorAxis)
).let{
  Line(
    BufferGeometry().setFromPoints(it.getPoints(segments)),
    LineDashedMaterial().apply {
      color = Color("white")
      dashSize = semiMajorAxis/10000
      gapSize = semiMajorAxis/5000
    }
  )
}.apply {
  position.x = -semiMajorAxis * eccentricity
  rotation.x = PI/2
  computeLineDistances()
  visible = false
}

fun Object3D.inclined(inclination: Double) = Object3D().also {
  it.add(this)
  rotation.z = inclination.degrees
}

class Orbit(
  val name: String,
  val body: Object3D,
  position: Object3D,
  val orbited: Object3D,
  val massOrbited: Double,
  val semiMajorAxis: Double,
  val eccentricity: Double,
  val inclination: Double
) {
  val position: Object3D
  val orbitLine: Object3D
  val orbit: Object3D
  init {
    this.position = position(position)
    orbitLine = makeOrbitLine(semiMajorAxis, eccentricity)
    orbit = Object3D().also {
      it.add(this.position)
      it.add(orbitLine)
    }.inclined(inclination)
  }
  fun position(body: Object3D) = Object3D().apply {
    position.x = semiMajorAxis * (1 - eccentricity)
    add(body)
  }
  val r: Number get() {
    orbited.updateWorldMatrix(true, true)
    val bodyPosition = Vector3().apply(position::getWorldPosition)
    val orbitingPosition = Vector3().apply(orbited::getWorldPosition)
    val distance = bodyPosition.distanceTo(orbitingPosition)
//        console.log("$name ${JSON.stringify(bodyPosition)} focus ${JSON.stringify(orbitingPosition)} d $distance")
    return distance
  }

  val μ get() = G * massOrbited

  fun v(r: Double) = sqrt(2 * μ / r / 1000.0 - μ / semiMajorAxis / 1000.0) / 1000.0

//    val cosE get() = cosE(eccentricity, cosϴ(semiMajorAxis, eccentricity, r.toDouble()))

  fun trueAnomaly(r: Double) = trueAnomaly(semiMajorAxis, eccentricity, r).let {
    if (position.position.y > 0.0) it
    else 2 * PI - it
  }

  fun flightPathAngle(trueAnomaly: Double) = flightPathAngle(eccentricity, trueAnomaly)

  fun vVector(r: Double): Vector3 {
    val v = v(r)
    val trueAnomaly = trueAnomaly(r)
    val flightPathAngle = flightPathAngle(trueAnomaly)
//        println("r $r v $v theta ${trueAnomaly * 180 / PI} gamma ${flightPathAngle * 180 / PI}")
    return Vector3(v * sin(trueAnomaly + flightPathAngle), 0.0, v * cos(trueAnomaly + flightPathAngle))
  }

  fun deltaPosition(tau: Double, elapsedTime: Double) {
    val E = keplerSolve(eccentricity, M(meanMotion(G * massOrbited, semiMajorAxis * 1000), tau, elapsedTime))
    val cosE = cos(E)
    val cosTheta = cosTheta(eccentricity, cosE)
    val r = r(semiMajorAxis, eccentricity, acos(cosTheta))
    val x = cosTheta * r
    val y = -sinTheta(eccentricity, cosE, sin(E)) * r
//        console.log("tau $tau t $elapsedTime r $r x $x y $y")
    position.position.set(x, 0.0, y)
    position.updateMatrix()
//        console.log("position ${JSON.stringify(body.position)}")
  }
}