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

import com.falsepattern.zigbuild.options.ZigBuildOptions
import org.apache.commons.io.FileUtils
import org.gradle.api.Action
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class ZigBuildTask: BaseZigTask<ZigBuildOptions> {
    @get:Internal
    abstract val workingDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val prefixDirectory: DirectoryProperty

    @get:Input
    abstract val clearPrefixDirectory: Property<Boolean>

    /**
     * This exists so that gradle can detect changes. Should match the contents of build.zig.zon -> paths
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection

    fun sourceFiles(action: Action<ConfigurableFileCollection>) {
        action.execute(sourceFiles)
    }

    fun sourceFiles(action: ConfigurableFileCollection.() -> Unit) {
        action.invoke(sourceFiles)
    }

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val libcFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val headers: ConfigurableFileCollection

    @get:Nested
    abstract override val options: ZigBuildOptions

    @Inject constructor() {
        val layout = project.layout
        buildFile.convention(layout.projectDirectory.file("build.zig"))
        workingDirectory.convention(layout.dir(buildFile.map { it.asFile.parentFile }))
        clearPrefixDirectory.convention(false)
    }

    @TaskAction
    fun run() {
        if (clearPrefixDirectory.get()) {
            val dir = prefixDirectory.asFile.get()
            if (dir.exists()) {
                FileUtils.deleteDirectory(dir)
            }
        }

        executeZig {
            workingDir = workingDirectory.get().asFile

            val args = ArrayList<String>()

            args.add("build")

            args.addAll(options.steps.get())

            args.addAll(options.cacheDirArgs.get())

            args.add("--prefix")
            args.add(prefixDirectory.get().asFile.absolutePath)

            args.add("--build-file")
            args.add(buildFile.get().asFile.absolutePath)

            if (libcFile.isPresent) {
                args.add("--libc")
                args.add(libcFile.get().asFile.absolutePath)
            }

            headers.forEach { headerDir ->
                args.add("--search-prefix")
                args.add(headerDir.absolutePath)
            }

            if (options.target.isPresent) {
                args.add("-Dtarget=${options.target.get().resolve()}")
            }

            if (options.optimize.isPresent) {
                args.add("-Doptimize=${options.optimize.get().name}")
            }

            args.addAll(options.compilerArgs.get())

            setArgs(args)
        }.rethrowFailure().assertNormalExitValue()
    }
}