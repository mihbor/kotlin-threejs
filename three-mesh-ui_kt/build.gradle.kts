plugins {
  kotlin("multiplatform") version "1.9.10"
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
        implementation(npm("three-mesh-ui", "4.6.0"))
      }
    }
  }
}