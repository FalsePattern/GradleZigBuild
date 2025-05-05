/*
 * ZigBuild
 *
 * Copyright (C) 2025 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

plugins {
    idea
    id("com.gradle.plugin-publish") version "1.3.1"
    `kotlin-dsl`
}

group = "com.falsepattern"
version = "0.1.1"

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.27.1")
    runtimeOnly("org.tukaani:xz:1.10")
}

val extraFiles = listOf("../LICENSE", "../COPYING", "../COPYING.LESSER", "../CREDITS", "../README.MD")

tasks.named<ProcessResources>("processResources") {
    from(extraFiles)
}

tasks.named<Jar>("sourcesJar") {
    from(extraFiles)
}

gradlePlugin {
    website.set("https://github.com/FalsePattern/GradleZigBuild")
    vcsUrl.set("https://github.com/FalsePattern/GradleZigBuild")
    plugins {
        create("zigbuild") {
            id = "com.falsepattern.zigbuild"
            implementationClass = "com.falsepattern.zigbuild.ZigPlugin"
            displayName = "Zig Build Integration"
            description = "A plugin for integrating with the zig build system and zig toolchain management."
            tags.set(listOf("zig", "zig build"))
        }
    }
}

// For staging builds
publishing {
    repositories {
        maven {
            name = "mavenpattern"
            setUrl("https://mvn.falsepattern.com/releases/")
        }
    }
}

// Reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}