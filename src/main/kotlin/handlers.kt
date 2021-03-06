import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Touch
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.get
import org.w3c.dom.pointerevents.PointerEvent
import three.js.Mesh
import three.js.Object3D
import three.js.Vector2
import three.js.Vector3
import kotlin.math.PI
import kotlin.math.sqrt

fun square(base: Int) = base * base

var touchStartX: Int? = null
var touchStartY: Int? = null
var touchDistance: Double? = null

var mouse = Vector2()

fun registerListeners() {
    val options = (js("{}") as Options).apply{
        passive = false
    }

    document.addEventListener("wheel", ::wheelHandler, options)
    document.addEventListener("click", ::clickHandler, false)
    document.addEventListener("touchstart", ::touchStartHandler, options)
    document.addEventListener("touchmove", ::touchMoveHandler, options)
    document.addEventListener("pointermove", ::pointerMoveHandler, options)
}

fun touchStartHandler(event: Event) {
    if (event is TouchEvent) {
        touchStartX = event.touches[0]?.pageX
        touchStartY = event.touches[0]?.pageY
        if (event.touches.length >= 2) {
            touchDistance = distance(event.touches[1]!!, event.touches[0]!!)
        }
    }
}

fun distance(touch1: Touch, touch2: Touch) = sqrt(
    square(touch1.pageX - touch2.pageX).toDouble()
    + square(touch1.pageY - touch2.pageY).toDouble()
)

fun touchMoveHandler(event: Event) {
    if (event is TouchEvent && touchStartX != null) {
        event.preventDefault()
        if (event.touches.length >= 2) {
            val distance = distance(event.touches[1]!!, event.touches[0]!!)
            zoom(touchDistance!! - distance)
            touchDistance = distance
        }
        val touchX = event.touches[0]!!.pageX
        val touchY = event.touches[0]!!.pageY
        val deltaX = touchX - touchStartX!!
        val deltaY = touchY - touchStartY!!
        cameraRotation.x += deltaY / PI / 200
        cameraRotation.y += deltaX / PI / 200
        touchStartX = touchX
        touchStartY = touchY
    }
}

fun pointerMoveHandler(event: Event) {
    if (event is PointerEvent) {
        mouse.x = 2.0 * event.clientX / window.innerWidth - 1
        mouse.y = 1 -2.0 * event.clientY / window.innerHeight
    }
//    console.log("pointermove ${JSON.stringify(mouse)}")
}
fun wheelHandler(event: Event) {
    if (event is WheelEvent) {
        event.preventDefault()
        if (event.ctrlKey) {
            zoom(event.deltaY)
        } else {
            cameraRotation.x += event.deltaY / PI / 100
            cameraRotation.y += event.deltaX / PI / 100
        }
    }
}

fun Object3D.hasNameInHierarchy(name: String): Boolean =
   if (this.name == name) true
   else this.parent != null && this.parent!!.hasNameInHierarchy(name)

fun Collection<Object3D>.hasObjectInHierarchy(obj: Object3D): Boolean =
    if (contains(obj)) true
    else obj.parent != null && this.hasObjectInHierarchy(obj.parent!!)

fun zoom(delta: Int) = zoom(delta.toDouble())

fun cameraPosition() = Vector3().apply(camera::getWorldPosition)

fun zoom(delta: Double) {
    val direction = Vector3();
    val focusedPosition = Vector3().apply(focused::getWorldPosition)
    camera.getWorldDirection(direction);
    val radius = radii[focused.name]!!
    val diff = direction.multiplyScalar((cameraPosition().sub(focusedPosition).length() - radius) * delta / -100)
    val newPosition = cameraPosition().add(diff)
//    console.log("Proposed distance to focused ${newPosition.clone().sub(focusedPosition).length()}")
    if (newPosition.clone().sub(focusedPosition).length() > radius * 1.05 // don't go into object
        && cameraPosition().sub(newPosition).length() < cameraPosition().sub(focusedPosition).length() // don't jump over object
        && newPosition.length() < 1e9) {
        camera.position.copy(newPosition)
        unfixAngleToFocused()
    }
}

fun clickHandler(event: Event) {
    if (event is MouseEvent) {
        event.preventDefault()
        val click = Vector2()
        val size = Vector2()
        renderer.getSize(size)
        click.x = 2.0 * event.clientX / size.x - 1
        click.y = 1 - 2.0 * event.clientY / size.y
        raycaster.setFromCamera(click, camera)
        val intersects = raycaster.intersectObjects(scene.children, true)
        val objects = intersects.map{it.`object`}
        buttonClicked(objects) ?: objectClicked(objects)
    }
}
fun objectClicked(intersects: List<Object3D>) = intersects.filterIsInstance<Mesh<*, *>>().firstOrNull(focusables::hasObjectInHierarchy)
    ?.let{ findAncestorInList(it, focusables) }
    ?.let(::focusOn)