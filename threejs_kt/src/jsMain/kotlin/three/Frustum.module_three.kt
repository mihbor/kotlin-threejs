@file:JsModule("three")
@file:JsNonModule
@file:Suppress("ABSTRACT_MEMBER_NOT_IMPLEMENTED", "VAR_TYPE_MISMATCH_ON_OVERRIDE", "INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS", "EXTERNAL_DELEGATION", "PackageDirectoryMismatch")
package three.js

open external class Frustum(p0: Plane = definedExternally, p1: Plane = definedExternally, p2: Plane = definedExternally, p3: Plane = definedExternally, p4: Plane = definedExternally, p5: Plane = definedExternally) {
    open var planes: Array<Plane>
    open fun set(p0: Plane, p1: Plane, p2: Plane, p3: Plane, p4: Plane, p5: Plane): Frustum
    open fun clone(): Frustum /* this */
    open fun copy(frustum: Frustum): Frustum /* this */
    open fun setFromProjectionMatrix(m: Matrix4): Frustum /* this */
    open fun intersectsObject(obj: Object3D): Boolean
    open fun intersectsSprite(sprite: Sprite): Boolean
    open fun intersectsSphere(sphere: Sphere): Boolean
    open fun intersectsBox(box: Box3): Boolean
    open fun containsPoint(point: Vector3): Boolean
}