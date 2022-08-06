import three.js.Object3D
import three.js.Raycaster
import three.js.Vector3

lateinit var focused: Object3D
val focusables = mutableListOf<Object3D>()

val raycaster = Raycaster().apply {
  far = 2e8
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

fun focusOn(obj: Object3D) {
  focused = obj
  issOrbit.orbitLine.visible = focused == issOrbit.body
  moonOrbit.orbitLine.visible = focused == moonOrbit.body
  earthOrbit.orbitLine.visible = focused == earthOrbit.body
  console.log("Focus now on ${focused.name}")
  cameraRotation.set(0, 0)
}