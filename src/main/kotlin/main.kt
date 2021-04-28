
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Window
import three.js.*
import three.js.loaders.GLTFLoader
import kotlin.math.PI

val earthRadius = 6378.137

val Window.aspectRatio get() = innerWidth.toDouble() / innerHeight

operator fun Number.minus(other: Number) = toDouble() - other.toDouble()
operator fun Number.plus(other: Number) = toDouble() + other.toDouble()
operator fun Number.times(other: Number) = toDouble() * other.toDouble()
operator fun Number.div(other: Number) = toDouble() / other.toDouble()
operator fun Number.compareTo(other: Number) = toDouble().compareTo(other.toDouble())

external fun require(module: String): dynamic

external interface Options {
    var passive: Boolean
}
val cameraRotation = Vector2(0, 0)
fun main() {

    window.onresize = {
        camera.aspect = window.aspectRatio
        camera.updateProjectionMatrix()

        renderer.setSize(window.innerWidth, window.innerHeight-4)
    }
    val options = (js("{}") as Options).apply{
        passive = false
    }
    document.addEventListener("wheel", ::wheelHandler, options)
    document.addEventListener("click", ::clickHandler, false)
    document.addEventListener("touchstart", ::touchStartHandler, options)
    document.addEventListener("touchmove", ::touchMoveHandler, options)
    val modelLoader = GLTFLoader()// = require("three/examples/jsm/loaders/GLTFLoader")
    modelLoader.load("iss/scene.gltf", {
        it.scene.name = "ISS"
        it.scene.position.z = earthRadius*10 - 10
        it.scene.scale.multiply(Vector3(0.01,0.01,0.01))
        scene.add(it.scene)
        console.log(it.scene)
    }, {}, console::log)

    animate()
}
fun animate() {
    val delta = clock.getDelta().toDouble()
    val focusedPosition = Vector3().apply(focused::getWorldPosition)

    earth.rotation.y += delta / PI / 5
    moon.rotation.y += delta / PI / 140
    moonOrbit.rotation.y += delta / PI / 140

    camera.lookAt(focusedPosition)
    camera.rotation.x += cameraRotation.x
    camera.rotation.y += cameraRotation.y

    renderer.render(scene, camera)

    window.requestAnimationFrame { animate() }
}

val clock = Clock()
val camera = PerspectiveCamera(60, window.aspectRatio, 0.5, 2e9).apply {
    position.z = earthRadius*10
}
val raycaster = Raycaster().apply {
    far = 2e8
}

val renderer = WebGLRenderer((js("{}") as WebGLRendererParameters).apply{ antialias = false }).apply {
    document.body?.appendChild(domElement)
    setSize(window.innerWidth, window.innerHeight-4)
    setPixelRatio(window.devicePixelRatio)
}

val texLoader = TextureLoader()
val earthTex = texLoader.load("1_earth_8k.jpg")
val moonTex = texLoader.load("8k_moon.jpg")
val starsTex = texLoader.load("tycho_skymap.jpg")
val earth = Mesh(SphereGeometry(earthRadius, 100, 100), MeshStandardMaterial().apply{ map = earthTex }).apply {
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
}

var focused: Mesh<SphereGeometry, *> = earth
val moon = Mesh(SphereGeometry(1738.1, 100, 100), MeshStandardMaterial().apply { map = moonTex }).apply {
    name = "Moon"
    position.x = -300e3
    rotation.y = -PI/4
    scale.y = 1736/1738.1
}
val moonOrbit = Object3D().apply{
    add(moon)
    rotation.y = -PI/4
}
val stars = Mesh(SphereGeometry(1e9, 30, 30), MeshBasicMaterial().apply {
    map = starsTex
    side = BackSide
})

val scene = Scene().apply {
    add(stars)
    add(earth)
    add(moonOrbit)

    add(DirectionalLight(0xffffff, 1).apply { position.set(5, 0.5, 5) })
}
