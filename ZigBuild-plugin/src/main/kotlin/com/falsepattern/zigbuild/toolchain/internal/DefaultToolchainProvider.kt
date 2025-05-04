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
import com.falsepattern.zigbuild.toolchain.ZigToolchainDownload
import com.falsepattern.zigbuild.toolchain.ZigToolchainProvider
import com.falsepattern.zigbuild.toolchain.ZigToolchainRequest
import com.falsepattern.zigbuild.toolchain.ZigVersion
import java.net.HttpURLConnection
import java.net.URI
import java.util.Optional

internal abstract class DefaultToolchainProvider: ZigToolchainProvider {
    companion object {
        val ZIG_DOWNLOAD_URL get() = URI("https://ziglang.org/download/")
    }

    override fun resolve(request: ZigToolchainRequest): Optional<ZigToolchainDownload> {
        val version = request.zigToolchainSpec.version.get().toString()
        val arch = ZigArchitectureTarget.current
        val os = ZigOperatingSystemTarget.current

        val ext = if (os == ZigOperatingSystemTarget.WINDOWS) "zip" else "tar.xz"

        val targetUri = ZIG_DOWNLOAD_URL.resolve("$version/zig-${os.name}-${arch.name}-$version.$ext")

        val targetUrl = targetUri.toURL()

        val conn = targetUrl.openConnection() as HttpURLConnection
        conn.requestMethod = "HEAD"
        if (conn.responseCode == 200) {
            return Optional.of(ZigToolchainDownload.of(targetUri, ZigVersion.of(version)))
        }

        return Optional.empty()
    }
}