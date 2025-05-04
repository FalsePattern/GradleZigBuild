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

import com.falsepattern.zigbuild.options.ZigOptions
import com.falsepattern.zigbuild.toolchain.ZigCompiler
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.process.ExecOperations
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class BaseZigTask<Options: ZigOptions>: DefaultTask {
    @get:Nested
    abstract val zigCompiler: Property<ZigCompiler>

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Internal
    abstract val options: Options

    @Inject
    constructor(): super() {
        options.zigCache.convention(project.layout.buildDirectory.dir("zig-cache/${name}"))
        group = "zig"
    }

    fun options(action: Action<Options>) {
        action.execute(options)
    }

    fun options(action: Options.() -> Unit) {
        action.invoke(options)
    }

    protected fun executeZig(callback: ExecSpec.() -> Unit): ExecResult = execOperations.exec {
        errorOutput = System.err
        standardOutput = System.out
        setExecutable(zigCompiler.get().executablePath)

        if (options.replaceEnv.get()) {
            setEnvironment(options.env.get())
        } else {
            environment(options.env.get())
        }

        callback.invoke(this)
    }
}