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

import org.gradle.api.Transformer
import org.gradle.api.invocation.Gradle
import java.io.File
import javax.inject.Inject

internal abstract class ToolchainUnpackTransform @Inject constructor(private val zigInfo: ResolvedZigToolchainInfo): Transformer<File, File> {
    @get:Inject
    protected abstract val gradle: Gradle

    override fun transform(file: File): File {
        val unpackingService = gradle.sharedServices.registrations.getByName(ToolchainUnpackingService.SERVICE_NAME).service.get() as ToolchainUnpackingService
        return unpackingService.unpack(file, zigInfo)
    }
}