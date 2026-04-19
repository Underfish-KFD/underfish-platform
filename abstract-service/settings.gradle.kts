pluginManagement {
    includeBuild("../buildSrc") {
        name = "build-logic"
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "abstract-service"

include(":utils")
project(":utils").projectDir = file("../utils")
