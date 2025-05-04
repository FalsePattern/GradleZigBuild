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

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.artifacts.repositories.AuthenticationContainer
import org.gradle.api.credentials.Credentials
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Property
import java.net.URI

interface ZigToolchainRepository: Named {
    fun credentials(action: Action<PasswordCredentials>) {
        credentials { action.execute(this) }
    }
    fun credentials(action: PasswordCredentials.() -> Unit)
    fun <T: Credentials> credentials(credentialsType: Class<out T>, action: Action<T>) {
        credentials(credentialsType) { action.execute(this) }
    }
    fun <T: Credentials> credentials(credentialsType: Class<out T>, action: T.() -> Unit)
    fun credentials(credentialsType: Class<out Credentials>)
    fun authentication(action: AuthenticationContainer.() -> Unit)
    val providerClass: Property<Class<out ZigToolchainProvider>>
    val rootUri: Property<URI>
}