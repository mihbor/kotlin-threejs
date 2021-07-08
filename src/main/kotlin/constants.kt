import kotlin.math.PI

val Double.degrees get() = this * PI /180

const val G = 6.674e-11
const val earthRadius = 6378.137
const val earthTilt = 23.44
const val earthOrbitRadius = 149598023.0 // = semimajor axis
const val earthOrbitEccentricity = 0.0167086
const val earthOrbitInclination = 7.155
const val earthMass = 5.972e24
const val moonRadius = 1738.1
const val moonTilt = 1.5
const val moonOrbitRadius = 384748.0 // = semimajor axis
const val moonOrbitEccentricity = 0.0549006
const val moonOrbitInclination = 5.145
const val issOrbitRadius = earthRadius + 420
const val issOrbitEccentricity = 0.0003458
const val sunRadius = 695700.0
const val sunMass = 1.989e30
val radii = mapOf("Sun" to sunRadius, "Earth" to earthRadius, "Moon" to moonRadius, "ISS" to 1.0)
