import three.js.*
import three.js.loaders.GLTFLoader
import kotlin.math.PI

val texLoader = TextureLoader()
val earthTex = texLoader.load("1_earth_8k.jpg")
val moonTex = texLoader.load("8k_moon.jpg")
val starsTex = texLoader.load("tycho_skymap.jpg")

val earth = createEarth().apply { focused = this }
val moon = createMoon()
val moonOrbit = Object3D().apply{
    add(moon)
    rotation.y = -PI/4
}

val issOrbit = Object3D().apply{
    rotation.x = 2 * PI * 51.64 / 360
}
val scene = createScene()
fun createScene() = Scene().apply {
    val stars = Mesh(SphereGeometry(1e9, 30, 30), MeshBasicMaterial().apply {
        map = starsTex
        side = BackSide
    })

    add(stars)
    add(earth)
    add(moonOrbit)
    add(issOrbit)
    loadISS()
    add(camera)

    add(DirectionalLight(0xffffff, 1).apply { position.set(5, 0.5, 5) })
}
fun createEarth() = Mesh(SphereGeometry(earthRadius, 100, 100), MeshStandardMaterial().apply{ map = earthTex }).apply {
    name = "Earth"
    rotation.y = PI
    rotation.x = 2 * PI * 23.44 / 360 // axial tilt
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
fun createMoon() = Mesh(SphereGeometry(moonRadius, 100, 100), MeshStandardMaterial().apply { map = moonTex }).apply {
    name = "Moon"
    position.x = -300e3
    rotation.y = -PI /4
    scale.y = 1736/1738.1
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
            iss.position.z = earthRadius + 420
            iss.scale.multiply(Vector3(0.01, 0.01, 0.01))
            issOrbit.add(iss)
            console.log(iss)
            focusables.add(iss)
        }, {}, console::log)
    }
}
