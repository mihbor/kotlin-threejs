import three.js.*
import three.js.loaders.GLTFLoader
import kotlin.math.PI

val texLoader = TextureLoader()
val earthTex = texLoader.load("1_earth_16k.jpg")
val moonTex = texLoader.load("8k_moon.jpg")
val sunTex = texLoader.load("8k_sun.jpg")
val starsTex = texLoader.load("tycho_skymap.jpg")

fun makeOrbitLine(radius: Number) = Line(
    CircleGeometry(radius, 1000).apply {
        vertices = vertices.drop(1).toTypedArray()
    },
    LineDashedMaterial().apply {
        color = Color("white")
        dashSize = 1
        gapSize = 2
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
val moon = createMoon()
val moonOrbitLine = makeOrbitLine(moonOrbitRadius)
val moonOrbit = Object3D().apply{
    add(moon)
    add(moonOrbitLine)
    rotation.y = -PI/4
}
fun inclinedOrbit(degrees: Double, orbit: Object3D) = Object3D().apply {
    rotation.z = 2 * PI * degrees / 360
    add(orbit)
}
val issOrbitRadius = earthRadius + 420
val issOrbitLine = makeOrbitLine(issOrbitRadius)
val issOrbit = Object3D().apply {
    add(issOrbitLine)
}
val scene = createScene()
fun createScene() = Scene().apply {
    val stars = Mesh(SphereGeometry(1e9, 30, 30), MeshBasicMaterial().apply {
        map = starsTex
        side = BackSide
    })

    add(stars)
    add(sun)
    add(earthAxialTilt)
    add(inclinedOrbit(5.145, moonOrbit))
    add(inclinedOrbit(51.64, issOrbit))
    loadISS()
    add(camera)

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
    position.x = earthOrbitRadius
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
