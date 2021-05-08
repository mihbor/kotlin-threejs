import three.js.Color
import three.js.Object3D
import three.mesh.ui.*

val buttons = mutableListOf<Block>()
fun createUI() = Block(BlockProps().apply {
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
    add(createButton("Earth"){earth})
    add(createButton("Moon"){moon})
    add(createButton("ISS"){iss})
}

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
fun createButton(name: String, obj: () -> Object3D) = Block(buttonOptions).apply {
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