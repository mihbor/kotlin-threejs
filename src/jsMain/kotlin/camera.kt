import three.js.Object3D
import three.js.Raycaster
import three.js.Vector3

var focused: Object3D? = null
val focusables = mutableListOf<Object3D>()

val raycaster = Raycaster().apply {
  far = 2e8
}
fun focusedPosition() = focused?.getWorldPosition(Vector3())

fun fixAngleToFocused() {
  focusedPosition()?.let {
    camera.lookAt(it)
    camera.rotation.x += cameraRotation.x
    camera.rotation.y += cameraRotation.y
  }
}

fun unfixAngleToFocused() {
  val camRotX = camera.rotation.x
  val camRotY = camera.rotation.y
  focusedPosition()?.let {
    camera.lookAt(it)
    cameraRotation.x = camRotX - camera.rotation.x
    cameraRotation.y = camRotY - camera.rotation.y
  }
}

fun focusOn(obj: Object3D) {
  focused = obj
  issOrbit.orbitLine.visible = focused == issOrbit.body
  moonOrbit.orbitLine.visible = focused == moonOrbit.body
  earthOrbit.orbitLine.visible = focused == earthOrbit.body
  console.log("Focus now on ${focused?.name}")
  cameraRotation.set(0, 0)
}