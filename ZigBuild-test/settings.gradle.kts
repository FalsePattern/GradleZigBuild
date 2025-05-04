pluginManagement {
    includeBuild("../ZigBuild-plugin")

    repositories {
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    id("com.falsepattern.zigbuild")
}
rootProject.name = "ZigBuild-test"
