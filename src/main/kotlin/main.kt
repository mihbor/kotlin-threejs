
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Window
import three.js.*
import three.js.loaders.GLTFLoader
import three.mesh.ui.*
import three.webxr.VRButton
import kotlin.math.PI
import kotlinx.serialization.json.Json

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

val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    allowSpecialFloatingPointValues = true
    useArrayPolymorphism = true
    allowStructuredMapKeys = true
    encodeDefaults = false
}
val clock = Clock()
val camera = PerspectiveCamera(60, window.aspectRatio, 0.5, 2e9).apply {
    position.z = earthRadius*10
}
val focusables = mutableListOf<Object3D>()
val buttons = mutableListOf<Block>()
val raycaster = Raycaster().apply {
    far = 2e8
}

val renderer = WebGLRenderer((js("{}") as WebGLRendererParameters).apply{ antialias = false }).apply {
    document.body?.appendChild( VRButton.createButton(this) )
    document.body?.appendChild(domElement)
    setSize(window.innerWidth, window.innerHeight-4)
    setPixelRatio(window.devicePixelRatio)
    xr.enabled = true
}

fun main() {
    window.onresize = {
        camera.aspect = window.aspectRatio
        camera.updateProjectionMatrix()

        renderer.setSize(window.innerWidth, window.innerHeight-4)
    }
    registerListeners()

    animate()
}

fun focusedPosition() = Vector3().apply(focused::getWorldPosition)

fun fixAngleToFocused() {
    camera.lookAt(focusedPosition())
    camera.rotation.x += cameraRotation.x
    camera.rotation.y += cameraRotation.y
}

fun unfixAngleToFocused() {
    val camRotX = camera.rotation.x
    val camRotY = camera.rotation.y
    camera.lookAt(focusedPosition())
    cameraRotation.x = camRotX - camera.rotation.x
    cameraRotation.y = camRotY - camera.rotation.y
}

fun animate() {
    val delta = clock.getDelta().toDouble()

    earth.rotation.y += delta / PI / 5
    moon.rotation.y += delta / PI / 140
    moonOrbit.rotation.y += delta / PI / 140

    fixAngleToFocused()

    ThreeMeshUI.update()

    renderer.render(scene, camera)

    updateButtons()

    window.requestAnimationFrame { animate() }
}

fun findAncestorInList(child: Object3D, list: List<Object3D>): Object3D? =
    if (list.contains(child)) child
    else child.parent?.let { findAncestorInList(it, list)}

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
    focusables.add(this)
}

var focused: Object3D = earth
val moonRadius = 1738.1
val moon = Mesh(SphereGeometry(moonRadius, 100, 100), MeshStandardMaterial().apply { map = moonTex }).apply {
    name = "Moon"
    position.x = -300e3
    rotation.y = -PI/4
    scale.y = 1736/1738.1
    focusables.add(this)
}
val moonOrbit = Object3D().apply{
    add(moon)
    rotation.y = -PI/4
}
lateinit var iss: Object3D
val modelLoader = GLTFLoader().apply {
    load("iss/scene.gltf", {
        iss = it.scene
        iss.name = "ISS"
        iss.position.z = camera.position.z - 5
        iss.scale.multiply(Vector3(0.01, 0.01, 0.01))
        scene.add(iss)
        console.log(iss)
        focusables.add(iss)
    }, {}, console::log)
}
val radii = mapOf("Earth" to earthRadius, "Moon" to moonRadius, "ISS" to 1.0)
val stars = Mesh(SphereGeometry(1e9, 30, 30), MeshBasicMaterial().apply {
    map = starsTex
    side = BackSide
})

val buttonOptions = BlockProps().apply {
    width = 0.25
    height = 0.1
    justifyContent = "center"
    alignContent = "center"
    offset = 0.05
    margin = 0.02
    borderRadius = 0.04
}
val hoveredStateAttributes = BlockState(
    state = "hovered",
    attributes = BlockProps().apply {
        offset = 0.035
        backgroundColor = Color(0x999999)
        backgroundOpacity = 1.0
        fontColor = Color(0xffffff)
    }
)
val idleStateAttributes = BlockState(
    state = "idle",
    attributes = BlockProps().apply {
        offset = 0.035
        backgroundColor = Color(0x666666)
        backgroundOpacity = 0.3
        fontColor = Color(0xffffff)
    }
)

val selectedAttributes = BlockProps().apply {
    offset = 0.02
    backgroundColor = Color(0x777777)
    fontColor = Color(0x222222)
}
fun makeButton(name: String, obj: () -> Object3D) = Block(buttonOptions).apply {
    add(Text(TextProps(name)))
    setupState(BlockState(
        state = "selected",
        attributes = selectedAttributes,
        onSet = {
            focused = obj.invoke()
            console.log("Focus now on ${focused.name}")
            cameraRotation.set(0, 0)
        }
    ))
    setupState(hoveredStateAttributes)
    setupState(idleStateAttributes)
    buttons.add(this)
}
val ui = Block(BlockProps().apply {
    justifyContent = "center"
    alignContent = "center"
    contentDirection = "column"
    padding = 0.02
    borderRadius = 0.11
    fontSize = 0.04
    fontFamily = "fonts/Roboto-msdf.json"
    fontTexture = "fonts/Roboto-msdf.png"
}).apply {
    add(Block(BlockProps().apply {
        fontSize = 0.04
        padding = 0.02
        width = 0.4
        height = 0.15
    }).apply{
        add(Text(TextProps("Click to focus:")))
    })
    camera.add(this)
    position.set(1, 0.7, -2)
    add(makeButton("Earth"){earth})
    add(makeButton("Moon"){moon})
    add(makeButton("ISS"){iss})
}
val scene = createScene()