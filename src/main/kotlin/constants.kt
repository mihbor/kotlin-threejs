
val G = 6.674e-11
val earthRadius = 6378.137
val earthOrbitRadius = 149598023.0 // = semimajor axis
val earthOrbitEccentricity = 0.0167086
val earthOrbitInclination = 7.155
val earthMass = 5.972e24
val moonRadius = 1738.1
val moonOrbitRadius = 384748.0 // = semimajor axis
val moonOrbitEccentricity = 0.0549006
val issOrbitRadius = earthRadius + 420
val sunRadius = 695700.0
val sunMass = 1.989e30
val radii = mapOf("Sun" to sunRadius, "Earth" to earthRadius, "Moon" to moonRadius, "ISS" to 1.0)
