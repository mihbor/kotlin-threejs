
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

    renderer.render(scene, camera)

    window.requestAnimationFrame { animate() }
}

operator fun Number.minus(other: Double) = toDouble() - other
operator fun Number.plus(other: Double) = toDouble() + other

val clock = Clock()
val camera = PerspectiveCamera(75, window.aspectRatio, 0.1, 1000).apply {
    position.z = 5
}

val renderer = WebGLRenderer().apply {
    document.body?.appendChild(domElement)
    setSize(window.innerWidth, window.innerHeight-4)
    setPixelRatio(window.devicePixelRatio)
}
val texLoader = TextureLoader()
val earthTex = texLoader.load("1_earth_16k.jpg")
val starsTex = texLoader.load("space.jpg")
@JsName("earth")
val earth = Mesh(SphereGeometry(2, 100, 100), MeshStandardMaterial().apply { map = earthTex }).apply {
    rotation.y = PI
}
@JsName("stars")
val stars = Mesh(SphereGeometry(100, 30, 30), MeshBasicMaterial().apply {
    map = starsTex
    side = BackSide
})

val scene = Scene().apply {
    add(stars)
    add(earth)

    add(DirectionalLight(0xffffff, 1).apply { position.set(5, 0.5, 5) })
//    add(AmbientLight(0x404040, 1))
}
