
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import three.js.*
import three.mesh.ui.TextProps
import three.mesh.ui.ThreeMeshUI
import three.webxr.VRButton
import kotlin.math.PI

val earthRadius = 6378.137
val earthOrbitRadius = 149598023.0 // = semimajor axis
val earthOrbitEccentricity = 0.0167086
val moonRadius = 1738.1
val moonOrbitRadius = 384748.0 // = semimajor axis
val moonOrbitEccentricity = 0.0549006
val issOrbitRadius = earthRadius + 420
val sunRadius = 695700.0
val radii = mapOf("Sun" to sunRadius, "Earth" to earthRadius, "Moon" to moonRadius, "ISS" to 1.0)

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

val renderer = WebGLRenderer((js("{}") as WebGLRendererParameters).apply{ antialias = false }).apply {
    document.body?.appendChild( VRButton.createButton(this) )
    document.body?.appendChild(domElement)
    setSize(window.innerWidth, window.innerHeight-4)
    setPixelRatio(window.devicePixelRatio)
    xr.enabled = false
}

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
var timeMultiplier = 60.0

val Number.daysPerRev
    get() = 2 * PI / 24 / 3600 / this.toDouble() * timeMultiplier
val Number.minutesPerRev
    get() = daysPerRev * 24 * 60

fun animate() {
    val delta = clock.getDelta().toDouble()

    earth.rotation.y += delta * 1.daysPerRev
    earthOrbit.rotation.y += delta * 365.daysPerRev
    moon.rotation.y += delta * 28.daysPerRev
    moonOrbit.rotation.y += delta * 28.daysPerRev
    issOrbit.rotation.y += delta * 92.68.minutesPerRev

    fixAngleToFocused()

    distanceText.set(TextProps(distanceToFocused().km
        + "\nearth: ${JSON.stringify(Vector3().apply(earth::getWorldPosition))}\ncamera: ${JSON.stringify(Vector3().apply(camera::getWorldPosition))}"))

    ThreeMeshUI.update()

    renderer.render(scene, camera)

    updateButtons()

    window.requestAnimationFrame { animate() }
}

fun findAncestorInList(child: Object3D, list: List<Object3D>): Object3D? =
    if (list.contains(child)) child
    else child.parent?.let { findAncestorInList(it, list)}

