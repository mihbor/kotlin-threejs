import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
  kotlin("multiplatform") version "2.2.10"
}

group = "me.mihbor"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  js(IR) {
    browser()
    binaries.executable()
    compilations["main"].packageJson {
      customField("homepage", "https://mihbor.github.io/kotlin-threejs")
    }
  }
  sourceSets {
    val jsMain by getting {
      dependencies {
        api(project(":threejs_kt"))
        api(project(":three-mesh-ui_kt"))
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-js:1.5.1")
      }
    }
  }
}

subprojects {
  group = rootProject.group
  version = rootProject.version

  plugins.withType(YarnPlugin::class.java) {
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
  }
}

plugins.withType(YarnPlugin::class.java) {
  rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}
