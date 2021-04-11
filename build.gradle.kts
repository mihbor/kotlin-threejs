plugins {
    kotlin("js") version "1.4.30"
}

group = "me.mihbor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":threejs_kt"))
    testImplementation(kotlin("test-js"))
}

kotlin {
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        compilations["main"].packageJson {
            customField("homepage", "https://mihbor.github.io/kotlin-threejs")
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}