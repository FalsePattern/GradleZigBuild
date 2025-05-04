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

package com.falsepattern.zigbuild.target

import com.falsepattern.zigbuild.target.internal.DefaultZigOperatingSystemTarget
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Named
import java.io.Serializable

interface ZigOperatingSystemTarget: Named, Serializable {
    companion object {
        // @formatter:off
        val FREESTANDING get() = of("freestanding")
        val OTHER        get() = of("other"       )
        val CONTIKI      get() = of("contiki"     )
        val ELFIAMCU     get() = of("elfiamcu"    )
        val FUCHSIA      get() = of("fuchsia"     )
        val HERMIT       get() = of("hermit"      )
        val AIX          get() = of("aix"         )
        val HAIKU        get() = of("haiku"       )
        val HURD         get() = of("hurd"        )
        val LINUX        get() = of("linux"       )
        val PLAN9        get() = of("plan9"       )
        val RTEMS        get() = of("rtems"       )
        val SERENITY     get() = of("serenity"    )
        val ZOS          get() = of("zos"         )
        val DRAGONFLY    get() = of("dragonfly"   )
        val FREEBSD      get() = of("freebsd"     )
        val NETBSD       get() = of("netbsd"      )
        val OPENBSD      get() = of("openbsd"     )
        val DRIVERKIT    get() = of("driverkit"   )
        val IOS          get() = of("ios"         )
        val MACOS        get() = of("macos"       )
        val TVOS         get() = of("tvos"        )
        val VISIONOS     get() = of("visionos"    )
        val WATCHOS      get() = of("watchos"     )
        val ILLUMOS      get() = of("illumos"     )
        val SOLARIS      get() = of("solaris"     )
        val WINDOWS      get() = of("windows"     )
        val UEFI         get() = of("uefi"        )
        val PS3          get() = of("ps3"         )
        val PS4          get() = of("ps4"         )
        val PS5          get() = of("ps5"         )
        val EMSCRIPTEN   get() = of("emscripten"  )
        val WASI         get() = of("wasi"        )
        val AMDHSA       get() = of("amdhsa"      )
        val AMDPAL       get() = of("amdpal"      )
        val CUDA         get() = of("cuda"        )
        val MESA3D       get() = of("mesa3d"      )
        val NVCL         get() = of("nvcl"        )
        val OPENCL       get() = of("opencl"      )
        val OPENGL       get() = of("opengl"      )
        val VULKAN       get() = of("vulkan"      )
        // @formatter:on
        fun of(os: String): ZigOperatingSystemTarget = DefaultZigOperatingSystemTarget(os)

        val current: ZigOperatingSystemTarget by lazy {
            if (SystemUtils.IS_OS_AIX) {
                return@lazy AIX
            }
            if (SystemUtils.IS_OS_LINUX) {
                return@lazy LINUX
            }
            if (SystemUtils.IS_OS_MAC_OSX) {
                return@lazy MACOS
            }
            if (SystemUtils.IS_OS_FREE_BSD) {
                return@lazy FREEBSD
            }
            if (SystemUtils.IS_OS_OPEN_BSD) {
                return@lazy OPENBSD
            }
            if (SystemUtils.IS_OS_NET_BSD) {
                return@lazy NETBSD
            }
            if (SystemUtils.IS_OS_SUN_OS || SystemUtils.IS_OS_SOLARIS) {
                return@lazy SOLARIS
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                return@lazy WINDOWS
            }
            if (SystemUtils.IS_OS_ZOS) {
                return@lazy ZOS
            }
            throw IllegalArgumentException("Unsupported operating system.")
        }
    }
}