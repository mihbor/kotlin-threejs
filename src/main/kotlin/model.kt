import three.js.Object3D
import three.js.Vector3

class OrbitParams(
    val body: Object3D,
    val orbiting: Object3D,
    val semiMajorAxis: Double,
    val eccentricity: Double,
    val inclination: Double
) {
    val distance: Number get() {
        val bodyPosition = Vector3().apply(body::getWorldPosition)
        val orbitingPosition = Vector3().apply(orbiting::getWorldPosition)
        return bodyPosition.distanceTo(orbitingPosition)
    }
}