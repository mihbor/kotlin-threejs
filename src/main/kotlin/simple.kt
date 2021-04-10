
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.WheelEvent
import three.js.*
import kotlin.math.PI

val Window.aspectRatio get() = innerWidth.toDouble() / innerHeight
external interface Options {
    var passive: Boolean
}
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

    animate()
}
fun wheelHandler(event: Event) {
    if (event is WheelEvent) {
        event.preventDefault()
        if (event.ctrlKey) {
            camera.position.z += event.deltaY / 100
        } else {
            camera.rotation.x += event.deltaY / PI / 100
            camera.rotation.y += event.deltaX / PI / 100
        }
    }
}
fun animate() {
    val delta = clock.getDelta().toDouble()

    earth.rotation.y += delta / PI / 10
    moon.rotation.y += delta / PI / 10
    moonOrbit.rotation.y += delta / PI / 10

    renderer.render(scene, camera)

    window.requestAnimationFrame { animate() }
}

operator fun Number.minus(other: Double) = toDouble() - other
operator fun Number.plus(other: Double) = toDouble() + other

val clock = Clock()
val camera = PerspectiveCamera(75, window.aspectRatio, 0.1, 2000).apply {
    position.z = 5
}

val renderer = WebGLRenderer().apply {
    document.body?.appendChild(domElement)
    setSize(window.innerWidth, window.innerHeight-4)
    setPixelRatio(window.devicePixelRatio)
}
val texLoader = TextureLoader()
val earthTex = texLoader.load("1_earth_16k.jpg")
val moonTex = texLoader.load("8k_moon.jpg")
val starsTex = texLoader.load("tycho_skymap.jpg")
val earth = Mesh(SphereGeometry(2, 100, 100), MeshStandardMaterial().apply { map = earthTex }).apply {
    rotation.y = PI
}
val moon = Mesh(SphereGeometry(1, 100, 100), MeshStandardMaterial().apply { map = moonTex }).apply {
    position.x = -10
    rotation.y = -PI/8
}
val moonOrbit = Object3D().apply{
    add(moon)
}
val stars = Mesh(SphereGeometry(1000, 30, 30), MeshBasicMaterial().apply {
    map = starsTex
    side = BackSide
})

val scene = Scene().apply {
    add(stars)
    add(earth)
    add(moonOrbit)

    add(DirectionalLight(0xffffff, 1).apply { position.set(5, 0.5, 5) })
}
