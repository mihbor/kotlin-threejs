@file:JsModule("three-mesh-ui")
@file:JsNonModule

import three.js.Object3D

@JsName("default")
external object ThreeMeshUI {
    fun update()
}

external interface BlockProps {
    var width: Double
    var height: Double
    var padding: Double
    var fontFamily: String
    var fontTexture: String
}

external class Block(options: BlockProps) : Object3D

external interface TextProps {
    var content: String
}
external class Text(options: TextProps) : Object3D
