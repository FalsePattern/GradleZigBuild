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

import com.falsepattern.zigbuild.target.ZigArchitectureTarget
import com.falsepattern.zigbuild.target.ZigOperatingSystemTarget
import com.falsepattern.zigbuild.toolchain.ZigVersion
import java.nio.file.Path
import java.util.Properties
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter

internal fun Path.readToolchainInfo(): ResolvedZigToolchainInfo? {
    val properties = Properties()
    bufferedReader().use { reader -> properties.load(reader) }
    val version = properties.getProperty("version") ?: return null
    val os = properties.getProperty("os") ?: return null
    val arch = properties.getProperty("arch") ?: return null
    return ResolvedZigToolchainInfo(ZigVersion.of(version), ZigOperatingSystemTarget.of(os), ZigArchitectureTarget.of(arch))
}

internal fun Path.writeToolchainInfo(info: ResolvedZigToolchainInfo) {
    val properties = Properties()
    properties["version"] = info.zigVersion.toString()
    properties["os"] = info.os.name
    properties["arch"] = info.arch.name
    bufferedWriter().use { writer -> properties.store(writer, "ZigBuild toolchain version info") }
}