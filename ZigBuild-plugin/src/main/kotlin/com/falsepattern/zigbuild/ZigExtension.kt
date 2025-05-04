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

package com.falsepattern.zigbuild

import com.falsepattern.zigbuild.toolchain.ZigCompiler
import com.falsepattern.zigbuild.toolchain.ZigToolchainSpec
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class ZigExtension {
    val toolchain: ZigToolchainSpec

    var scanSystem: Boolean = true

    @get:Inject
    protected abstract val objectFactory: ObjectFactory

    @Inject
    constructor() {
        toolchain = objectFactory.newInstance<ZigToolchainSpec>()
    }

    fun toolchain(action: Action<ZigToolchainSpec>) {
        action.execute(toolchain)
    }

    fun toolchain(action: ZigToolchainSpec.() -> Unit) {
        action.invoke(toolchain)
    }

    fun compilerFor(config: Action<ZigToolchainSpec>): Provider<ZigCompiler> {
        val spec = objectFactory.newInstance<ZigToolchainSpec>()
        spec.version.convention(toolchain.version)
        config.execute(spec)
        return compilerFor(spec)
    }

    fun compilerFor(config: ZigToolchainSpec.() -> Unit): Provider<ZigCompiler> {
        val spec = objectFactory.newInstance<ZigToolchainSpec>()
        spec.version.convention(toolchain.version)
        config.invoke(spec)
        return compilerFor(spec)
    }

    abstract fun compilerFor(spec: ZigToolchainSpec): Provider<ZigCompiler>
}