import org.w3c.dom.Touch
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.get
import three.js.*
import kotlin.math.PI
import kotlin.math.sqrt

var touchStartX: Int? = null
var touchStartY: Int? = null
var touchDistance: Int? = null

fun square(base: Int) = base * base
fun sqrt(number: Int) = sqrt(number.toDouble()).toInt()

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
    square(touch1.pageX - touch2.pageX)
    + square(touch1.pageY - touch2.pageY)
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

fun zoom(delta: Int) = zoom(delta.toDouble())

fun zoom(delta: Double) {
    val direction = Vector3();
    val focusedPosition = Vector3().apply(focused::getWorldPosition)
    camera.getWorldDirection(direction);
    val radius = if (focused.hasNameInHierarchy("ISS")) 1 else focused.geometry.parameters.radius
    val newPosition = camera.position.clone().add(
        direction.multiplyScalar((camera.position.clone().sub(focusedPosition).length() - radius) * delta / -100)
    )
    if (newPosition.clone().sub(focusedPosition).length() > radius * 1.05 && newPosition.length() < 1e8) {
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
        click.x = 2 * event.clientX / size.x - 1
        click.y = 1 - 2 * event.clientY / size.y
        raycaster.setFromCamera(click, camera)
        val intersects = raycaster.intersectObjects(scene.children, true)
        intersects.map{it.`object`}
            .firstOrNull{it.name.isNotBlank()}
            ?.let {
                focused = it as Mesh<SphereGeometry, *>
                console.log("Focus now on ${focused.name}")
                cameraRotation.set(0, 0)
            }
    }
}