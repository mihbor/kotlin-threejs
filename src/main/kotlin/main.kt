
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import three.js.*
import three.mesh.ui.TextProps
import three.mesh.ui.ThreeMeshUI
import three.webxr.VRButton
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

val Window.aspectRatio get() = innerWidth.toDouble() / innerHeight

operator fun Number.minus(other: Number) = toDouble() - other.toDouble()
operator fun Number.plus(other: Number) = toDouble() + other.toDouble()
operator fun Number.times(other: Number) = toDouble() * other.toDouble()
operator fun Number.div(other: Number) = toDouble() / other.toDouble()
operator fun Number.compareTo(other: Number) = toDouble().compareTo(other.toDouble())

fun semiMinorAxis(e: Number, a: Number) = sqrt(1 - e.toDouble().pow(2)) * a

external fun require(module: String): dynamic

external interface Options {
    var passive: Boolean
}
val cameraRotation = Vector2(0, 0)

val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    useArrayPolymorphism = true
    allowStructuredMapKeys = true
    encodeDefaults = false
}
val clock = Clock()
val camera = PerspectiveCamera(60, window.aspectRatio, 0.5, 2e9)

val renderer = WebGLRenderer((js("{}") as WebGLRendererParameters).apply{
    antialias = false
    logarithmicDepthBuffer = false
}).apply {
    document.body?.appendChild( VRButton.createButton(this) )
    document.body?.appendChild(domElement)
    setSize(window.innerWidth, window.innerHeight-4)
    setPixelRatio(window.devicePixelRatio)
    xr.enabled = false
}

var t0 = clock.elapsedTime.toDouble()
var elapsedTime = 0.0

fun main() {
    window.onresize = {
        camera.aspect = window.aspectRatio
        camera.updateProjectionMatrix()

        renderer.setSize(window.innerWidth, window.innerHeight-4)
    }
    createControls()
    createTimeControls()
    createCoordinateDisplay()
    registerListeners()

    animate()
}
var timeMultiplier = 1.0

val Number.daysPerRev
    get() = 2 * PI / 24 / 3600 / this.toDouble()

val Number.minutesPerRev
    get() = daysPerRev * 24 * 60

fun animate() {
    val delta = clock.getDelta().toDouble() * timeMultiplier
    elapsedTime += delta

    earth.rotation.y += delta * 1.daysPerRev
    earthOrbit.deltaPosition(t0, elapsedTime)
    moon.rotation.y += delta * 28.daysPerRev
    moonOrbit.deltaPosition(t0, elapsedTime)
    issOrbit.deltaPosition(t0, elapsedTime)

    fixAngleToFocused()

    val orbitOfFocused = focused.let { it.userData["orbit"] as Orbit? }

    distanceText.set(TextProps(distanceToFocused().km
//        + "\nearth: ${JSON.stringify(Vector3().apply(earth::getWorldPosition))}\ncamera: ${JSON.stringify(Vector3().apply(camera::getWorldPosition))}"
        + (orbitOfFocused?.run {
            val r = this.r.toDouble()
            "\n${name} orbiting ${orbited.name}\nr: ${r.km}\nv: ${v(r).km}/s"
        } ?: "")
    ))

    ThreeMeshUI.update()

    renderer.render(scene, camera)

    updateButtons()

    window.requestAnimationFrame { animate() }
}

fun findAncestorInList(child: Object3D, list: List<Object3D>): Object3D? =
    if (list.contains(child)) child
    else child.parent?.let { findAncestorInList(it, list)}

