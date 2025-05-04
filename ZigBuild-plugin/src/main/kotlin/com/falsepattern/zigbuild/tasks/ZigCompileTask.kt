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

import com.falsepattern.zigbuild.options.ZigCompileOptions
import com.falsepattern.zigbuild.options.ZigCompileOptions.ArtifactType
import org.apache.commons.io.FileUtils
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class ZigCompileTask @Inject constructor(): BaseZigTask<ZigCompileOptions>() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:SkipWhenEmpty
    abstract val sourceFiles: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val headers: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    protected val headerFiles get() = headers.asFileTree

    @get:Input
    abstract val baseArtifactName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Nested
    abstract override val options: ZigCompileOptions

    @TaskAction
    fun run() {
        val dir = outputDirectory.asFile.get()
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir)
        }
        dir.mkdirs()

        executeZig {
            workingDir(outputDirectory.get())
            val args = ArrayList<String>()
            args.add(when(options.artifactType.get()) {
                ArtifactType.Library -> "build-lib"
                ArtifactType.Executable -> "build-exe"
                ArtifactType.Object -> "build-obj"
            })

            args.addAll(options.cacheDirArgs.get())

            args.add("--name")
            args.add(baseArtifactName.get())

            // Module settings

            if (options.dynamic.isPresent) {
                if (options.dynamic.get()) {
                    args.add("-dynamic")
                } else {
                    args.add("-static")
                }
            }

            // If not set, use building system's target (zig automatically detects this, no need to go through BuildPlatform)

            if (options.target.isPresent) {
                val target = options.target.get()
                args.add("-target")
                args.add(target.resolve())
            }

            args.addAll(options.compilerArgs.get())

            for (headerDir in headers.files) {
                args.add("-I${headerDir.absolutePath}")
            }

            for (sourceFile in sourceFiles.files) {
                args.add(sourceFile.absolutePath)
            }

            setArgs(args)
        }.rethrowFailure().assertNormalExitValue()
    }
}