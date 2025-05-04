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

import com.falsepattern.zigbuild.target.internal.DefaultZigAbiTarget
import org.gradle.api.Named
import java.io.Serializable

interface ZigAbiTarget: Named, Serializable {
    companion object {
        // @formatter:off
        val NONE        get() = of("none"       )
        val GNU         get() = of("gnu"        )
        val GNUABIN32   get() = of("gnuabin32"  )
        val GNUABI64    get() = of("gnuabi64"   )
        val GNUEABI     get() = of("gnueabi"    )
        val GNUEABIHF   get() = of("gnueabihf"  )
        val GNUF32      get() = of("gnuf32"     )
        val GNUSF       get() = of("gnusf"      )
        val GNUX32      get() = of("gnux32"     )
        val GNUILP32    get() = of("gnuilp32"   )
        val CODE16      get() = of("code16"     )
        val EABI        get() = of("eabi"       )
        val EABIHF      get() = of("eabihf"     )
        val ILP32       get() = of("ilp32"      )
        val ANDROID     get() = of("android"    )
        val ANDROIDEABI get() = of("androideabi")
        val MUSL        get() = of("musl"       )
        val MUSLABIN32  get() = of("muslabin32" )
        val MUSLABI64   get() = of("muslabi64"  )
        val MUSLEABI    get() = of("musleabi"   )
        val MUSLEABIHF  get() = of("musleabihf" )
        val MUSLX32     get() = of("muslx32"    )
        val MSVC        get() = of("msvc"       )
        val ITANIUM     get() = of("itanium"    )
        val CYGNUS      get() = of("cygnus"     )
        val SIMULATOR   get() = of("simulator"  )
        val MACABI      get() = of("macabi"     )
        val OHOS        get() = of("ohos"       )
        val OHOSEABI    get() = of("ohoseabi"   )
        // @formatter:on
        fun of(abi: String): ZigAbiTarget = DefaultZigAbiTarget(abi)
    }
}