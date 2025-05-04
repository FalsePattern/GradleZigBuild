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

import com.falsepattern.zigbuild.toolchain.ZigToolchainProvider
import org.gradle.api.provider.Provider
import java.net.URI

@JvmRecord
internal data class ZigToolchainProviderInfo(val info: SerializedInfo, val provider: Provider<out ZigToolchainProvider>) {
    @JvmRecord
    data class SerializedInfo(val name: String, val rootUri: URI?)
}
