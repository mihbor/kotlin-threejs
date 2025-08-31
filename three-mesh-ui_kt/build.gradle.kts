plugins {
  kotlin("multiplatform") version "2.2.10"
}

repositories {
  mavenCentral()
}

kotlin {
  js(IR) {
    browser()
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        api(project(":threejs_kt"))
        implementation(npm("three-mesh-ui", "6.5.4"))
      }
    }
  }
}
