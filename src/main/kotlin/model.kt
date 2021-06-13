import three.js.Object3D
import three.js.Vector3
import kotlin.math.sqrt

class OrbitParams(
    val body: Object3D,
    val orbited: Object3D,
    val massOrbited: Double,
    val semiMajorAxis: Double,
    val eccentricity: Double,
    val inclination: Double
) {
    val r: Number get() {
        val bodyPosition = Vector3().apply(body::getWorldPosition)
        val orbitingPosition = Vector3().apply(orbited::getWorldPosition)
        return bodyPosition.distanceTo(orbitingPosition)
    }

    val μ get() = G * massOrbited

    val v get() = sqrt(2 * μ / r / 1000 - μ / semiMajorAxis / 1000) / 1000
}