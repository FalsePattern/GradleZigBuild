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

import com.falsepattern.zigbuild.internal.ZigExtensionInternal
import com.falsepattern.zigbuild.tasks.BaseZigTask
import com.falsepattern.zigbuild.toolchain.ZigToolchainProvider
import com.falsepattern.zigbuild.toolchain.ZigToolchainRepository
import com.falsepattern.zigbuild.toolchain.ZigToolchainsManagement
import com.falsepattern.zigbuild.toolchain.internal.DefaultToolchainProvider
import com.falsepattern.zigbuild.toolchain.internal.ToolchainUnpackingService
import com.falsepattern.zigbuild.toolchain.internal.ZigToolchainProviderInfo
import com.falsepattern.zigbuild.toolchain.internal.ZigToolchainRepositoryInternal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.ComponentMetadataHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.initialization.Settings
import org.gradle.api.initialization.resolve.RepositoriesMode
import org.gradle.api.initialization.resolve.RulesMode
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import javax.inject.Inject

abstract class ZigPlugin @Inject constructor(): Plugin<Any> {
    override fun apply(target: Any) {
        when(target) {
            is Project -> applyProject(target)
            is Settings -> applySettings(target)
            is Gradle -> {}
            else -> throw IllegalArgumentException("ZigPlugin can only be applied to a Project or Settings.")
        }
    }

    private fun applyProject(project: Project) {
        val zigExtension = project.extensions.create(ZigExtension::class, "zig", ZigExtensionInternal::class)
        project.gradle.sharedServices.registerIfAbsent(ToolchainUnpackingService.SERVICE_NAME, ToolchainUnpackingService::class.java) {
            parameters.gradleUserHome.set(project.gradle.gradleUserHomeDir)
        }
        project.tasks.withType<BaseZigTask<*>> {
            zigCompiler.convention(zigExtension.compilerFor{})
        }
    }

    private fun applySettings(settings: Settings) {
        settings.gradle.pluginManager.apply(ZigPlugin::class.java)

        val repositories = settings.toolchainManagement.extensions.create<ZigToolchainsManagement>("zig").zigRepositories

        // Add the default provider
        repositories.register("zig-default") {
            rootUri.set(DefaultToolchainProvider.ZIG_DOWNLOAD_URL)
            providerClass.set(DefaultToolchainProvider::class.java)
        }

        settings.gradle.settingsEvaluated {
            val failOnProjectRepos = dependencyResolutionManagement.repositoriesMode.map { it == RepositoriesMode.FAIL_ON_PROJECT_REPOS }
//            val failOnProjectRules = dependencyResolutionManagement.rulesMode.map { it == RulesMode.FAIL_ON_PROJECT_RULES }

            val services = ArrayList<ZigToolchainProviderInfo.SerializedInfo>()
            for (toolchainRepo in repositories) {
                settings.gradle.sharedServices.registerIfAbsent("${ToolchainUnpackingService.ZIG_TOOLCHAIN_PROVIDER_SERVICE_PREFIX}.${toolchainRepo.name}", toolchainRepo.providerClass.get())
                services.add(ZigToolchainProviderInfo.SerializedInfo(toolchainRepo.name, toolchainRepo.rootUri.get()))
            }

            gradle.lifecycle.beforeProject {
                if (!failOnProjectRepos.get()) {
                    applyToolchainRepos(project.repositories, repositories)
                }
//                if (!failOnProjectRules.get()) {
//                    applyToolchainModules(project.dependencies.components, repositories)
//                }
                val zigExtension = project.extensions.findByType<ZigExtension>()
                if (zigExtension != null) {
                    (zigExtension as ZigExtensionInternal).setProviders(services)
                }
                project.extensions.add(ZigExtensionInternal.ZIG_TOOLCHAIN_PROVIDER_PROXY_EXTENSION, services)
            }

            applyToolchainRepos(dependencyResolutionManagement.repositories, repositories)
//            applyToolchainModules(dependencyResolutionManagement.components, repositories)
        }
    }

    companion object {
        private fun applyToolchainRepos(repositoryHandler: RepositoryHandler, repos: Collection<ZigToolchainRepository>): Unit = with(repositoryHandler) {
            for (toolchainRepo in repos) {
                exclusiveContent {
                    forRepository {
                        ivy {
                            name = "zigbuild-toolchain-repository-${toolchainRepo.name}"
                            (toolchainRepo as ZigToolchainRepositoryInternal).apply(this)
                            setUrl(toolchainRepo.rootUri)
                            patternLayout {
                                artifact("[revision]")
                            }
                            metadataSources {
                                artifact()
                            }
                        }
                    }
                    filter {
                        includeModule("${ToolchainUnpackingService.ZIG_TOOLCHAIN_PROVIDER_SERVICE_PREFIX}.${toolchainRepo.name}", "zig")
                    }
                }
            }
        }

//        private fun applyToolchainModules(components: ComponentMetadataHandler, repos: Collection<ZigToolchainRepository>) {
//
//        }
    }
}