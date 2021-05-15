import three.js.Color
import three.js.Object3D
import three.js.Vector3
import three.mesh.ui.*

val buttons = mutableListOf<Block>()
val uiProps = BlockProps().apply {
    justifyContent = "center"
    alignContent = "center"
    contentDirection = "column"
    padding = 0.02
    borderRadius = 0.05
    fontSize = 0.04
    fontFamily = "fonts/Roboto-msdf.json"
    fontTexture = "fonts/Roboto-msdf.png"
}
fun Double.format(digits: Int): String = this.asDynamic().toFixed(digits)

val Number.km
    get() = toDouble().let{ if (it < 1000) it.format(3) else it.format(0)}  + "km"

val distanceText = Text(TextProps(distanceToFocused().km))

fun distanceToFocused(): Number {
    val focusedPosition = Vector3().apply(focused::getWorldPosition)
    val cameraPosition = Vector3().apply(camera::getWorldPosition)
    return cameraPosition.distanceTo(focusedPosition)
}

fun createCoordinateDisplay() = Block(uiProps).apply {
    add(Block(BlockProps().apply {
        width = 0.4
        height = 0.1
        backgroundOpacity = 0.0
    }).apply {
        add(Text(TextProps("Distance to centre:\n")))
        add(distanceText)
    })
    camera.add(this)
    position.set(1, -0.7, -2)
}
fun createControls() = Block(uiProps).apply {
    add(Block(BlockProps().apply {
        width = 0.4
        height = 0.1
        backgroundOpacity = 0.0
    }).apply{
        add(Text(TextProps("Click to focus:")))
    })
    camera.add(this)
    position.set(1, 0.7, -2)
    add(createFocusButton("Sun"){ sun })
    add(createFocusButton("Earth"){ earth })
    add(createFocusButton("Moon"){ moon })
    add(createFocusButton("ISS"){ iss })
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
fun createFocusButton(name: String, obj: () -> Object3D) = Block(buttonOptions).apply {
    add(Text(TextProps(name)))
    setupState(BlockState(
        state = "selected",
        attributes = selectedAttributes,
        onSet = { focusOn(obj.invoke()) }
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

fun buttonClicked(intersects: List<Object3D>): Block? {
    console.log("Something clicked")
    return intersects.getOrNull(0)
        ?.let { findAncestorInList(it, buttons) as Block? }
        ?.apply { setState("selected") }
}