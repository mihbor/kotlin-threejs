
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.WheelEvent
import three.js.*
import kotlin.math.PI

val earthRadius = 6378.137

val Window.aspectRatio get() = innerWidth.toDouble() / innerHeight

operator fun Number.minus(other: Double) = toDouble() - other
operator fun Number.minus(other: Number) = toDouble() - other.toDouble()
operator fun Number.plus(other: Double) = toDouble() + other
operator fun Number.times(other: Double) = toDouble() * other
operator fun Number.compareTo(other: Double) = toDouble().compareTo(other)

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
fun animate() {
    val delta = clock.getDelta().toDouble()

    earth.rotation.y += delta / PI / 10
    moon.rotation.y += delta / PI / 10
    moonOrbit.rotation.y += delta / PI / 10

    renderer.render(scene, camera)

    window.requestAnimationFrame { animate() }
}
fun wheelHandler(event: Event) {
    if (event is WheelEvent) {
        event.preventDefault()
        if (event.ctrlKey) {
            val direction = Vector3();
            camera.getWorldDirection( direction );
            val radius = focused.geometry.parameters.radius
            val newPosition = camera.position.clone().add(direction.multiplyScalar((camera.position.clone().sub(focused.position).length() - focused.geometry.parameters.radius) * event.deltaY / -100))
            if (newPosition.length() > radius + 100.0) {
                camera.position.copy(newPosition)
            }
        } else {
            camera.rotation.x += event.deltaY / PI / 100
            camera.rotation.y += event.deltaX / PI / 100
        }
    }
}

val clock = Clock()
val camera = PerspectiveCamera(75, window.aspectRatio, 1, 2e9).apply {
    position.z = earthRadius*2
}

val renderer = WebGLRenderer((js("{}") as WebGLRendererParameters).apply{ antialias = false }).apply {
    document.body?.appendChild(domElement)
    setSize(window.innerWidth, window.innerHeight-4)
    setPixelRatio(window.devicePixelRatio)
}
val texLoader = TextureLoader()
val earthTex = texLoader.load("1_earth_16k.jpg")
val moonTex = texLoader.load("8k_moon.jpg")
val starsTex = texLoader.load("tycho_skymap.jpg")
val earth = Mesh(SphereGeometry(earthRadius, 100, 100), MeshStandardMaterial().apply{ map = earthTex }).apply {
    rotation.y = PI
    scale.y = 6356.752/ earthRadius
    repeat(10) {
        val atmosphere = Mesh(SphereGeometry(earthRadius + 20 + 10*it, 100, 100), MeshStandardMaterial().apply {
            color = Color(0xffffff)
            transparent = true
            opacity = 0.05
        })
        add(atmosphere)
    }
}

var focused = earth
val moon = Mesh(SphereGeometry(1738.1, 100, 100), MeshStandardMaterial().apply { map = moonTex }).apply {
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
