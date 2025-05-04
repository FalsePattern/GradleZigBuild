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

package com.falsepattern.zigbuild.internal

import com.falsepattern.zigbuild.ZigExtension
import com.falsepattern.zigbuild.toolchain.ZigCompiler
import com.falsepattern.zigbuild.toolchain.ZigToolchainProvider
import com.falsepattern.zigbuild.toolchain.ZigToolchainSpec
import com.falsepattern.zigbuild.toolchain.internal.ToolchainUnpackingService
import com.falsepattern.zigbuild.toolchain.internal.ZigToolchain
import com.falsepattern.zigbuild.toolchain.internal.ZigToolchainProviderInfo
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

internal abstract class ZigExtensionInternal: ZigExtension {
    @get:Inject
    protected abstract val project: Project

    protected abstract val toolchainProviderInfo: ListProperty<ZigToolchainProviderInfo>

    @Suppress("UNCHECKED_CAST")
    @Inject
    constructor(): super() {
        val providerIntoList = project.extensions.findByName(ZIG_TOOLCHAIN_PROVIDER_PROXY_EXTENSION) as? List<ZigToolchainProviderInfo.SerializedInfo>
        if (providerIntoList != null) {
            setProviders(providerIntoList)
        }
    }

    fun setProviders(providerServices: List<ZigToolchainProviderInfo.SerializedInfo>) {
        for (providerInfo in providerServices) {
            val providerService = project.gradle.sharedServices.registrations.findByName("${ToolchainUnpackingService.ZIG_TOOLCHAIN_PROVIDER_SERVICE_PREFIX}.${providerInfo.name}")
            if (providerService == null) {
                continue
            }
            toolchainProviderInfo.add(ZigToolchainProviderInfo(providerInfo, providerService.service.map { it as ZigToolchainProvider }))
        }
    }

    private fun toolchainFor(spec: ZigToolchainSpec): Provider<ZigToolchain> {
        val service = project.gradle.sharedServices.registrations.getByName(ToolchainUnpackingService.SERVICE_NAME).service.get() as ToolchainUnpackingService
        return service.toolchainFor(spec, project, toolchainProviderInfo, scanSystem)
    }

    override fun compilerFor(spec: ZigToolchainSpec): Provider<ZigCompiler> {
        return toolchainFor(spec).map(objectFactory.newInstance<CompilerCreatingTransformer>())
    }

    companion object {
        const val ZIG_TOOLCHAIN_PROVIDER_PROXY_EXTENSION = "com.falsepattern.zigbuild.internal.toolchain.providersExtension"
    }

    abstract class CompilerCreatingTransformer @Inject constructor(): Transformer<ZigCompiler, ZigToolchain> {
        @get:Inject
        protected abstract val objectFactory: ObjectFactory

        override fun transform(zigToolchain: ZigToolchain): ZigCompiler {
            val metadata = objectFactory.newInstance<ZigToolchain.DefaultInstallationMetadata>(zigToolchain)
            return objectFactory.newInstance<ZigToolchain.DefaultZigCompiler>(metadata)
        }
    }
}