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

package com.falsepattern.zigbuild.toolchain.internal

import com.falsepattern.zigbuild.toolchain.ZigCompiler
import com.falsepattern.zigbuild.toolchain.ZigInstallationMetadata
import com.falsepattern.zigbuild.toolchain.ZigVersion
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.internal.os.OperatingSystem
import javax.inject.Inject

internal open class ZigToolchain @Inject constructor(@get:Internal
                                                     val installationPath: Directory,
                                                     @get:Nested
                                                     val spec: ResolvedZigToolchainInfo) {
    internal abstract class DefaultInstallationMetadata @Inject constructor(toolchain: ZigToolchain): ZigInstallationMetadata {
        override val version: ZigVersion = toolchain.spec.zigVersion
        override val installationPath: Directory = toolchain.installationPath
    }

    internal abstract class DefaultZigCompiler @Inject constructor(override val installationMetadata: ZigInstallationMetadata): ZigCompiler {
        override val executablePath: RegularFile get() {
            val os = OperatingSystem.current()
            val exeName: String = if (os.isWindows) {
                "zig.exe"
            } else {
                "zig"
            }
            return installationMetadata.installationPath.file(exeName)
        }
    }
}