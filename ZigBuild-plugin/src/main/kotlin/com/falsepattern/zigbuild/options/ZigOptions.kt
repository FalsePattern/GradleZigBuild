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

package com.falsepattern.zigbuild.options

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import javax.inject.Inject

abstract class ZigOptions {
    @get:Input
    abstract val env: MapProperty<String, String>

    @get:Input
    @get:Optional
    abstract val replaceEnv: Property<Boolean>

    @get:Input
    abstract val compilerArgs: ListProperty<String>

    @get:Internal
    abstract val zigCache: DirectoryProperty

    @get:Internal
    abstract val globalZigCache: DirectoryProperty

    @get:Inject
    abstract val gradle: Gradle

    @get:Inject
    abstract val objectFactory: ObjectFactory

    @Inject
    constructor() {
        replaceEnv.convention(false)
    }

    @get:Internal
    val cacheDirArgs: Provider<List<String>> get() {
        val args = objectFactory.listProperty<String>()
        args.addAll(zigCache.map { listOf("--cache-dir", it.asFile.absolutePath) }.orElse(emptyList()))
        args.addAll(globalZigCache.map { listOf("--global-cache-dir", it.asFile.absolutePath) }.orElse(emptyList()))
        return args
    }
}