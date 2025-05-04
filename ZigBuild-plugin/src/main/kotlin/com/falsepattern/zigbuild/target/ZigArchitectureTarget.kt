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

import com.falsepattern.zigbuild.target.internal.DefaultZigArchitectureTarget
import org.apache.commons.lang3.ArchUtils
import org.apache.commons.lang3.arch.Processor
import org.gradle.api.Named
import java.io.Serializable

interface ZigArchitectureTarget: Named, Serializable {
    companion object {
        // @formatter:off
        val AMDGCN      get() = of("amdgcn"     )
        val ARC         get() = of("arc"        )
        val ARM         get() = of("arm"        )
        val ARMEB       get() = of("armeb"      )
        val THUMB       get() = of("thumb"      )
        val THUMBEB     get() = of("thumbeb"    )
        val AARCH64     get() = of("aarch64"    )
        val AARCH64_BE  get() = of("aarch64_be" )
        val AVR         get() = of("avr"        )
        val BPFEL       get() = of("bpfel"      )
        val BPFEB       get() = of("bpfeb"      )
        val CSKY        get() = of("csky"       )
        val HEXAGON     get() = of("hexagon"    )
        val KALIMBA     get() = of("kalimba"    )
        val LANAI       get() = of("lanai"      )
        val LOONGARCH32 get() = of("loongarch32")
        val LOONGARCH64 get() = of("loongarch64")
        val M68K        get() = of("m68k"       )
        val MIPS        get() = of("mips"       )
        val MIPSEL      get() = of("mipsel"     )
        val MIPS64      get() = of("mips64"     )
        val MIPS64EL    get() = of("mips64el"   )
        val MSP430      get() = of("msp430"     )
        val NVPTX       get() = of("nvptx"      )
        val NVPTX64     get() = of("nvptx64"    )
        val POWERPC     get() = of("powerpc"    )
        val POWERPCLE   get() = of("powerpcle"  )
        val POWERPC64   get() = of("powerpc64"  )
        val POWERPC64LE get() = of("powerpc64le")
        val PROPELLER   get() = of("propeller"  )
        val RISCV32     get() = of("riscv32"    )
        val RISCV64     get() = of("riscv64"    )
        val S390X       get() = of("s390x"      )
        val SPARC       get() = of("sparc"      )
        val SPARC64     get() = of("sparc64"    )
        val SPIRV       get() = of("spirv"      )
        val SPIRV32     get() = of("spirv32"    )
        val SPIRV64     get() = of("spirv64"    )
        val VE          get() = of("ve"         )
        val WASM32      get() = of("wasm32"     )
        val WASM64      get() = of("wasm64"     )
        val X86         get() = of("x86"        )
        val X86_64      get() = of("x86_64"     )
        val XCORE       get() = of("xcore"      )
        val XTENSA      get() = of("xtensa"     )
        // @formatter:on
        fun of(arch: String): ZigArchitectureTarget = DefaultZigArchitectureTarget(arch)

        val current: ZigArchitectureTarget by lazy {
            val processor = ArchUtils.getProcessor()
            when(processor.type) {
                Processor.Type.AARCH_64 -> return@lazy AARCH64
                Processor.Type.X86 -> when(processor.arch) {
                    Processor.Arch.BIT_32 -> return@lazy X86
                    Processor.Arch.BIT_64 -> return@lazy X86_64
                    else -> {}
                }
                Processor.Type.PPC -> when(processor.arch) {
                    Processor.Arch.BIT_32 -> return@lazy POWERPC
                    Processor.Arch.BIT_64 -> return@lazy POWERPC64
                    else -> {}
                }
                Processor.Type.RISC_V -> when(processor.arch) {
                    Processor.Arch.BIT_32 -> return@lazy RISCV32
                    Processor.Arch.BIT_64 -> return@lazy RISCV64
                    else -> {}
                }
                else -> {}
            }
            throw IllegalArgumentException("Unsupported CPU architecture.")
        }
    }
}