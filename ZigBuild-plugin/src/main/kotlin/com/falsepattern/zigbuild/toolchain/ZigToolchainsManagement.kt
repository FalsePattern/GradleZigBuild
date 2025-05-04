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

package com.falsepattern.zigbuild.toolchain

import com.falsepattern.zigbuild.toolchain.internal.ZigToolchainRepositoryInternal
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import javax.inject.Inject

abstract class ZigToolchainsManagement {
    val zigRepositories: NamedDomainObjectContainer<ZigToolchainRepository>

    @get:Inject
    protected abstract val objectFactory: ObjectFactory

    @Inject
    constructor() {
        zigRepositories = objectFactory.domainObjectContainer(ZigToolchainRepository::class.java){ name -> objectFactory.newInstance<ZigToolchainRepositoryInternal>(name)}
    }

    fun zigRepositories(action: Action<NamedDomainObjectContainer<ZigToolchainRepository>>) {
        action.execute(zigRepositories)
    }

    fun zigRepositories(action: NamedDomainObjectContainer<ZigToolchainRepository>.() -> Unit) {
        action.invoke(zigRepositories)
    }
}