import three.js.*
import three.js.loaders.GLTFLoader
import kotlin.math.PI

val texLoader = TextureLoader()
val earthTex = texLoader.load("1_earth_16k.jpg")
val moonTex = texLoader.load("8k_moon.jpg")
val sunTex = texLoader.load("8k_sun.jpg")
val starsTex = texLoader.load("tycho_skymap.jpg")

fun makeOrbitLine(radius: Number, segments: Int = 1000) = Line(
    CircleGeometry(radius, segments).apply {
        vertices = (vertices.drop(1) as MutableList).apply{ add(get(0)) }.toTypedArray()
    },
    LineDashedMaterial().apply {
        color = Color("white")
        dashSize = radius/10000
        gapSize = radius/5000
    }
).apply {
    rotation.x = PI/2
    computeLineDistances()
    visible = false
}
val sun = createSun()
val earth = createEarth().apply { focused = this }

val earthAxialTilt = Object3D().apply {
    add(earth)
    rotation.x = 2 * PI * 23.44 / 360 // axial tilt
}
val earthOrbitLine = makeOrbitLine(earthOrbitRadius)
var earthOrbitalPosition = Object3D().apply {
    position.x = earthOrbitRadius
    add(earthAxialTilt)
}
val earthOrbit = Object3D().apply {
    position.x = earthOrbitRadius * earthOrbitEccentricity
    add(earthOrbitalPosition)
    add(earthOrbitLine)
}
val moon = createMoon()
val moonOrbitLine = makeOrbitLine(moonOrbitRadius)
val moonOrbit = Object3D().apply{
    position.x = moonOrbitRadius * moonOrbitEccentricity
    add(moon)
    add(moonOrbitLine)
    rotation.y = -PI/4
}
fun inclinedOrbit(degrees: Double, orbit: Object3D) = Object3D().apply {
    rotation.z = 2 * PI * degrees / 360
    add(orbit)
}
val issOrbitLine = makeOrbitLine(issOrbitRadius)
val issOrbit = Object3D().apply {
    add(issOrbitLine)
}

val stars = Mesh(SphereGeometry(1e9, 30, 30), MeshBasicMaterial().apply {
    map = starsTex
    side = BackSide
})
val scene = createScene()
fun createScene() = Scene().apply {
    add(stars)
    earthOrbitalPosition.add(inclinedOrbit(5.145, moonOrbit))
    earthOrbitalPosition.add(inclinedOrbit(51.64, issOrbit))
    sun.add(inclinedOrbit(7.155, earthOrbit))
    add(sun)
    loadISS()
    add(camera.apply {
        earth.updateWorldMatrix(true, false)
        console.log("Initial camera position: ${JSON.stringify(camera.position)}")
        earth.getWorldPosition(camera.position)
        console.log("Camera moved to Earth position: ${JSON.stringify(camera.position)}")
        position.z += earthRadius*3
        console.log("Camera offset 3x Earth radius position: ${JSON.stringify(camera.position)}")
        camera.lookAt(Vector3().apply(earth::getWorldPosition))
    })
}
fun createEarth() = Mesh(SphereGeometry(earthRadius, 100, 100), MeshStandardMaterial().apply{
    map = earthTex
}).apply {
    name = "Earth"
    rotation.y = PI
    scale.y = 6356.752/ earthRadius
    val n = 10
    repeat(n) {
        val atmosphere = Mesh(SphereGeometry(earthRadius + 20 + (100/n)*it, 100, 100), MeshStandardMaterial().apply {
            color = Color(0xffffff)
            transparent = true
            opacity = 0.5/n
            scale.y = 6356.752/ earthRadius
        })
        add(atmosphere)
    }
    focusables.add(this)
}
fun createMoon() = Mesh(SphereGeometry(moonRadius, 100, 100), MeshStandardMaterial().apply {
    map = moonTex
}).apply {
    name = "Moon"
    position.x = -moonOrbitRadius
    rotation.y = -PI /4
    scale.y = 1736/1738.1
    focusables.add(this)
}
fun createSun() = Mesh(SphereGeometry(sunRadius, 100, 100), MeshStandardMaterial().apply {
    emissiveMap = sunTex
//    color = Color("yellow")
    emissive = Color("yellow")
    emissiveIntensity = 2
}).apply {
    name = "Sun"
    add(PointLight(0xffffff, 1))
    val n = 0
    repeat(n) {
        val atmosphere = Mesh(SphereGeometry(sunRadius + 20000 + (100000/n)*it, 100, 100), MeshStandardMaterial().apply {
            color = Color("yellow")
            transparent = true
            opacity = 0.5/n
            side = DoubleSide
        })
        add(atmosphere)
    }
    focusables.add(this)
}
lateinit var iss: Object3D
fun loadISS() {
    GLTFLoader().apply {
        load("iss/scene.gltf", {
            iss = it.scene
            iss.rotation.y = PI/2
            iss.rotation.z = PI/2
            iss.name = "ISS"
            iss.position.z = issOrbitRadius
            iss.scale.multiply(Vector3(0.01, 0.01, 0.01))
            issOrbit.add(iss)
            console.log(iss)
            focusables.add(iss)
        }, {}, console::log)
    }
}
