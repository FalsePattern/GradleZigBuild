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

package com.falsepattern.zigbuild.tasks

import com.falsepattern.zigbuild.options.ZigOptions
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class ZigTask: BaseZigTask<ZigOptions> {
    @get:Internal
    abstract val workingDirectory: DirectoryProperty

    @get:Nested
    abstract override val options: ZigOptions

    @Inject
    constructor() {
        workingDirectory.convention(project.layout.projectDirectory)
    }

    @TaskAction
    fun run() {
        executeZig {
            workingDir = workingDirectory.get().asFile
            args = options.compilerArgs.get()
        }.rethrowFailure().assertNormalExitValue()
    }
}