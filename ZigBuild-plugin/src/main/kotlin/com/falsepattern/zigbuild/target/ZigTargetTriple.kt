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

import com.falsepattern.zigbuild.target.internal.DefaultZigTargetTriple
import java.io.Serializable

interface ZigTargetTriple: Serializable {
    val arch: ZigArchitectureTarget
    val os: ZigOperatingSystemTarget
    val abi: ZigAbiTarget?

    fun resolve(): String

    companion object {
        // @formatter:off
        val ARC_LINUX_GNU             get() = of(ZigArchitectureTarget.ARC        , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val ARM_LINUX_GNUEABI         get() = of(ZigArchitectureTarget.ARM        , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABI   )
        val ARM_LINUX_GNUEABIHF       get() = of(ZigArchitectureTarget.ARM        , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABIHF )
        val ARM_LINUX_MUSLEABI        get() = of(ZigArchitectureTarget.ARM        , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABI  )
        val ARM_LINUX_MUSLEABIHF      get() = of(ZigArchitectureTarget.ARM        , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABIHF)
        val ARMEB_LINUX_GNUEABI       get() = of(ZigArchitectureTarget.ARMEB      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABI   )
        val ARMEB_LINUX_GNUEABIHF     get() = of(ZigArchitectureTarget.ARMEB      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABIHF )
        val ARMEB_LINUX_MUSLEABI      get() = of(ZigArchitectureTarget.ARMEB      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABI  )
        val ARMEB_LINUX_MUSLEABIHF    get() = of(ZigArchitectureTarget.ARMEB      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABIHF)
        val THUMB_LINUX_MUSLEABI      get() = of(ZigArchitectureTarget.THUMB      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABI  )
        val THUMB_LINUX_MUSLEABIHF    get() = of(ZigArchitectureTarget.THUMB      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABIHF)
        val THUMB_WINDOWS_GNU         get() = of(ZigArchitectureTarget.THUMB      , ZigOperatingSystemTarget.WINDOWS, ZigAbiTarget.GNU       )
        val THUMBEB_LINUX_MUSLEABI    get() = of(ZigArchitectureTarget.THUMBEB    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABI  )
        val THUMBEB_LINUX_MUSLEABIHF  get() = of(ZigArchitectureTarget.THUMBEB    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABIHF)
        val AARCH64_LINUX_GNU         get() = of(ZigArchitectureTarget.AARCH64    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val AARCH64_LINUX_MUSL        get() = of(ZigArchitectureTarget.AARCH64    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val AARCH64_MACOS_NONE        get() = of(ZigArchitectureTarget.AARCH64    , ZigOperatingSystemTarget.MACOS  , ZigAbiTarget.NONE      )
        val AARCH64_WINDOWS_GNU       get() = of(ZigArchitectureTarget.AARCH64    , ZigOperatingSystemTarget.WINDOWS, ZigAbiTarget.GNU       )
        val AARCH64_BE_LINUX_GNU      get() = of(ZigArchitectureTarget.AARCH64_BE , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val AARCH64_BE_LINUX_MUSL     get() = of(ZigArchitectureTarget.AARCH64_BE , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val CSKY_LINUX_GNUEABI        get() = of(ZigArchitectureTarget.CSKY       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABI   )
        val CSKY_LINUX_GNUEABIHF      get() = of(ZigArchitectureTarget.CSKY       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABIHF )
        val LOONGARCH64_LINUX_GNU     get() = of(ZigArchitectureTarget.LOONGARCH64, ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val LOONGARCH64_LINUX_GNUSF   get() = of(ZigArchitectureTarget.LOONGARCH64, ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUSF     )
        val LOONGARCH64_LINUX_MUSL    get() = of(ZigArchitectureTarget.LOONGARCH64, ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val M68K_LINUX_GNU            get() = of(ZigArchitectureTarget.M68K       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val M68K_LINUX_MUSL           get() = of(ZigArchitectureTarget.M68K       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val MIPS_LINUX_GNUEABI        get() = of(ZigArchitectureTarget.MIPS       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABI   )
        val MIPS_LINUX_GNUEABIHF      get() = of(ZigArchitectureTarget.MIPS       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABIHF )
        val MIPS_LINUX_MUSLEABI       get() = of(ZigArchitectureTarget.MIPS       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABI  )
        val MIPS_LINUX_MUSLEABIHF     get() = of(ZigArchitectureTarget.MIPS       , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABIHF)
        val MIPSEL_LINUX_GNUEABI      get() = of(ZigArchitectureTarget.MIPSEL     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABI   )
        val MIPSEL_LINUX_GNUEABIHF    get() = of(ZigArchitectureTarget.MIPSEL     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABIHF )
        val MIPSEL_LINUX_MUSLEABI     get() = of(ZigArchitectureTarget.MIPSEL     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABI  )
        val MIPSEL_LINUX_MUSLEABIHF   get() = of(ZigArchitectureTarget.MIPSEL     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABIHF)
        val MIPS64_LINUX_GNUABI64     get() = of(ZigArchitectureTarget.MIPS64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUABI64  )
        val MIPS64_LINUX_GNUABIN32    get() = of(ZigArchitectureTarget.MIPS64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUABIN32 )
        val MIPS64_LINUX_MUSLABI64    get() = of(ZigArchitectureTarget.MIPS64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLABI64 )
        val MIPS64_LINUX_MUSLABIN32   get() = of(ZigArchitectureTarget.MIPS64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLABIN32)
        val MIPS64EL_LINUX_GNUABI64   get() = of(ZigArchitectureTarget.MIPS64EL   , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUABI64  )
        val MIPS64EL_LINUX_GNUABIN32  get() = of(ZigArchitectureTarget.MIPS64EL   , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUABIN32 )
        val MIPS64EL_LINUX_MUSLABI64  get() = of(ZigArchitectureTarget.MIPS64EL   , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLABI64 )
        val MIPS64EL_LINUX_MUSLABIN32 get() = of(ZigArchitectureTarget.MIPS64EL   , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLABIN32)
        val POWERPC_LINUX_GNUEABI     get() = of(ZigArchitectureTarget.POWERPC    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABI   )
        val POWERPC_LINUX_GNUEABIHF   get() = of(ZigArchitectureTarget.POWERPC    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUEABIHF )
        val POWERPC_LINUX_MUSLEABI    get() = of(ZigArchitectureTarget.POWERPC    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABI  )
        val POWERPC_LINUX_MUSLEABIHF  get() = of(ZigArchitectureTarget.POWERPC    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLEABIHF)
        val POWERPC64_LINUX_GNU       get() = of(ZigArchitectureTarget.POWERPC64  , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val POWERPC64_LINUX_MUSL      get() = of(ZigArchitectureTarget.POWERPC64  , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val POWERPC64LE_LINUX_GNU     get() = of(ZigArchitectureTarget.POWERPC64LE, ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val POWERPC64LE_LINUX_MUSL    get() = of(ZigArchitectureTarget.POWERPC64LE, ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val RISCV32_LINUX_GNU         get() = of(ZigArchitectureTarget.RISCV32    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val RISCV32_LINUX_MUSL        get() = of(ZigArchitectureTarget.RISCV32    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val RISCV64_LINUX_GNU         get() = of(ZigArchitectureTarget.RISCV64    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val RISCV64_LINUX_MUSL        get() = of(ZigArchitectureTarget.RISCV64    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val S390X_LINUX_GNU           get() = of(ZigArchitectureTarget.S390X      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val S390X_LINUX_MUSL          get() = of(ZigArchitectureTarget.S390X      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val SPARC_LINUX_GNU           get() = of(ZigArchitectureTarget.SPARC      , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val SPARC64_LINUX_GNU         get() = of(ZigArchitectureTarget.SPARC64    , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val WASM32_WASI_MUSL          get() = of(ZigArchitectureTarget.WASM32     , ZigOperatingSystemTarget.WASI   , ZigAbiTarget.MUSL      )
        val X86_LINUX_GNU             get() = of(ZigArchitectureTarget.X86        , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val X86_LINUX_MUSL            get() = of(ZigArchitectureTarget.X86        , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val X86_WINDOWS_GNU           get() = of(ZigArchitectureTarget.X86        , ZigOperatingSystemTarget.WINDOWS, ZigAbiTarget.GNU       )
        val X86_64_LINUX_GNU          get() = of(ZigArchitectureTarget.X86_64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNU       )
        val X86_64_LINUX_GNUX32       get() = of(ZigArchitectureTarget.X86_64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.GNUX32    )
        val X86_64_LINUX_MUSL         get() = of(ZigArchitectureTarget.X86_64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSL      )
        val X86_64_LINUX_MUSLX32      get() = of(ZigArchitectureTarget.X86_64     , ZigOperatingSystemTarget.LINUX  , ZigAbiTarget.MUSLX32   )
        val X86_64_MACOS_NONE         get() = of(ZigArchitectureTarget.X86_64     , ZigOperatingSystemTarget.MACOS  , ZigAbiTarget.NONE      )
        val X86_64_WINDOWS_GNU        get() = of(ZigArchitectureTarget.X86_64     , ZigOperatingSystemTarget.WINDOWS, ZigAbiTarget.GNU       )

        // @formatter:on
        fun of(arch: ZigArchitectureTarget, os: ZigOperatingSystemTarget, abi: ZigAbiTarget?): ZigTargetTriple = DefaultZigTargetTriple(arch, os, abi)
    }
}