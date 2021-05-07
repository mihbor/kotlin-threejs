import three.js.Object3D
import three.mesh.ui.Block


fun updateButtons() {
//    console.log("pointermove ${JSON.stringify(mouse)}")
    if (mouse.x != null && mouse.y != null) {
        raycaster.setFromCamera(mouse, camera)
        val intersects = raycaster.intersectObjects(buttons.toTypedArray(), true)
        val intersected = intersects.getOrNull(0) ?.`object`
            ?.let{ findAncestorInList(it, buttons) as Block? }
            ?.apply{ setState("hovered") }

        buttons.filter { it != intersected }.forEach { it.setState("idle") }
    }
}

fun buttonClicked(intersects: List<Object3D>) {
    console.log("Something clicked")
    intersects.getOrNull(0)
        ?.let { findAncestorInList(it, buttons) as Block? }
        ?.apply { setState("selected") }
}