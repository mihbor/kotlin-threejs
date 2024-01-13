import three.js.*
import three.js.loaders.GLTFLoader
import kotlin.math.PI

val texLoader = TextureLoader()
val starsTex = texLoader.load("tycho_skymap.jpg")
val earthTex = texLoader.load("1_earth_16k.jpg")
val moonTex = texLoader.load("8k_moon.jpg")
val sunTex = texLoader.load("8k_sun.jpg")

val sun = createSun()
val earth = createEarth().apply { focused = this }
val earthAxiallyTilted = earth.tilted(earthTilt.degrees)
val earthOrbit = Orbit("Earth", earth, earthAxiallyTilted, sun, sunMass, earthOrbitRadius, earthOrbitEccentricity, earthOrbitInclination).apply {
  earth.userData.set("orbit", this)
}
val moon = createMoon()
val moonAxiallyTilted = moon.tilted(moonTilt.degrees)
val moonOrbit = Orbit("Moon", moon, moonAxiallyTilted, earth, earthMass, moonOrbitRadius, moonOrbitEccentricity, moonOrbitInclination).apply {
  moon.userData.set("orbit", this)
}
fun Object3D.tilted(tilt: Double) = Object3D().also {
  it.add(this)
  rotation.x = tilt.degrees
}

val iss = Object3D()
val issOrbit = Orbit("ISS", iss, Object3D(), earth, earthMass, issOrbitRadius, issOrbitEccentricity, issOrbitInclination)

val stars = Mesh(SphereGeometry(1e9, 30, 30), MeshBasicMaterial().apply {
  map = starsTex
  side = BackSide
  color = Color(0.2, 0.2, 0.2)
})
val scene = createScene()
fun createScene() = Scene().apply {
  add(stars)
  earthOrbit.position.add(moonOrbit.orbit)
  earthOrbit.position.add(issOrbit.orbit)
  sun.add(earthOrbit.orbit)
  add(sun)
  loadISS()
  add(camera.apply {
    earth.updateWorldMatrix(true, true)
    console.log("Initial camera position: ${JSON.stringify(camera.position)}")
    earth.getWorldPosition(camera.position)
    console.log("Camera moved to Earth position: ${JSON.stringify(camera.position)}")
    position.z += earthRadius*3
    console.log("Camera offset 3x Earth radius position: ${JSON.stringify(camera.position)}")
    camera.lookAt(Vector3().apply(earth::getWorldPosition))
  })
}
fun createEarth() = Mesh(SphereGeometry(earthRadius, 100, 100), MeshStandardMaterial().apply{
  map = earthTex
}).apply {
  name = "Earth"
  rotation.y = PI
  scale.y = 6356.752/ earthRadius
  val n = 10
  repeat(n) {
    val atmosphere = Mesh(SphereGeometry(earthRadius + 20 + (100/n)*it, 100, 100), MeshStandardMaterial().apply {
      color = Color(0xffffff)
      transparent = true
      opacity = 0.5/n
      scale.y = 6356.752/ earthRadius
    })
    add(atmosphere)
  }
  focusables.add(this)
}
fun createMoon() = Mesh(SphereGeometry(moonRadius, 100, 100), MeshStandardMaterial().apply {
  map = moonTex
}).apply {
  name = "Moon"
  rotation.y = -PI /4
  scale.y = 1736/1738.1
  focusables.add(this)
}
fun createSun() = Mesh(SphereGeometry(sunRadius, 100, 100), MeshStandardMaterial().apply {
  emissiveMap = sunTex
//    color = Color("yellow")
  emissive = Color("yellow")
  emissiveIntensity = 2
}).apply {
  name = "Sun"
  add(PointLight(0xffffee, 3, 1e9, 0))
//  val n = 1
//  repeat(n) {
//    val atmosphere = Mesh(SphereGeometry(sunRadius + 20000 + (100000/n)*it, 100, 100), MeshStandardMaterial().apply {
//      color = Color("yellow")
//      transparent = true
//      opacity = 0.5/n
//      side = DoubleSide
//    })
//    add(atmosphere)
//  }
  focusables.add(this)
}
fun loadISS() {
  GLTFLoader().load("iss/scene.gltf", {
      iss.add(it.scene)
      iss.rotation.y = PI/2
      iss.rotation.z = PI/2
      iss.name = "ISS"
      iss.scale.multiply(Vector3(0.01, 0.01, 0.01))
      issOrbit.position.add(iss)
      iss.userData.set("orbit", issOrbit)
      console.log(iss)
      focusables.add(iss)
    }, {}, console::log)
}
