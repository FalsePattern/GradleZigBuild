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
import com.falsepattern.zigbuild.toolchain.ZigToolchainRepository
import org.gradle.api.Action
import org.gradle.api.artifacts.repositories.AuthenticationContainer
import org.gradle.api.artifacts.repositories.AuthenticationSupported
import org.gradle.api.credentials.Credentials
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.net.URI
import javax.inject.Inject

internal abstract class ZigToolchainRepositoryInternal @Inject constructor(private val name: String): ZigToolchainRepository {
    private var credentialsType: Class<out Credentials> = PasswordCredentials::class.java
    private val credentialsActions = ArrayList<Any>()
    private val credentialsActionsTypes = ArrayList<Class<out Credentials>>()
    private val authenticationActions = ArrayList<AuthenticationContainer.() -> Unit>()

    @get:Inject
    protected abstract val objectFactory: ObjectFactory

    @Suppress("UNCHECKED_CAST")
    fun apply(supported: AuthenticationSupported) {
        if (credentialsActions.isNotEmpty()) {
            supported.credentials(credentialsType) {
                for (action in credentialsActions) {
                    (action as Credentials.() -> Unit).invoke(this)
                }
            }
        }
        if (authenticationActions.isNotEmpty()) {
            supported.authentication {
                for (action in authenticationActions) {
                    action.invoke(this)
                }
            }
        }
    }

    override fun getName() = name

    override fun credentials(action: PasswordCredentials.() -> Unit) {
        if (!PasswordCredentials::class.java.isAssignableFrom(credentialsType)) {
            throw IllegalStateException("Credentials type is not PasswordCredentials")
        }
        credentialsActions.add(action)
        credentialsActionsTypes.add(PasswordCredentials::class.java)
    }

    override fun <T : Credentials> credentials(credentialsType: Class<out T>, action: T.() -> Unit) {
        credentials(credentialsType)
        if (!credentialsType.isAssignableFrom(this.credentialsType)) {
            throw IllegalStateException("Credentials type is not ${credentialsType.name}")
        }
        credentialsActions.add(action)
        credentialsActionsTypes.add(credentialsType)
    }

    override fun credentials(credentialsType: Class<out Credentials>) {
        this.credentialsType = credentialsType
        for (type in credentialsActionsTypes) {
            if (!credentialsType.isAssignableFrom(type)) {
                throw IllegalStateException("Credentials type is not ${type.name}")
            }
        }
    }

    override fun authentication(action: AuthenticationContainer.() -> Unit) {
        this.authenticationActions.add(action)
    }

    abstract override val providerClass: Property<Class<out ZigToolchainProvider>>
    abstract override val rootUri: Property<URI>
}