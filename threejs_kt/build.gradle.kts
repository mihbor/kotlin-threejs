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
        implementation(npm("three", "^0.120.0", generateExternals = false))
      }
    }
  }
}