import three.js.DirectionalLight
import three.js.Scene

fun createScene() = Scene().apply {
    add(stars)
    add(earth)
    add(moonOrbit)
    add(camera)

    add(DirectionalLight(0xffffff, 1).apply { position.set(5, 0.5, 5) })
}