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

import com.falsepattern.zigbuild.ZigExtension
import com.falsepattern.zigbuild.target.ZigArchitectureTarget
import com.falsepattern.zigbuild.target.ZigOperatingSystemTarget
import com.falsepattern.zigbuild.toolchain.ZigToolchainRequest
import com.falsepattern.zigbuild.toolchain.ZigToolchainSpec
import com.falsepattern.zigbuild.toolchain.ZigVersion
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.services.ServiceReference
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.process.ExecOutput
import org.gradle.process.ExecResult
import org.gradle.process.ExecSpec
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.AutoCloseable
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Stream
import javax.inject.Inject
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isExecutable
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString
import kotlin.runCatching

internal abstract class ToolchainUnpackingService: BuildService<ToolchainUnpackingService.Parameters> {
    @Inject
    constructor()

    @get:Inject
    protected abstract val objectFactory: ObjectFactory

    @get:Inject
    protected abstract val providerFactory: ProviderFactory

    private val managedToolchains: MutableList<ResolvedZigToolchain> = ArrayList()
    private val unmanagedToolchains: MutableList<ResolvedZigToolchain> = ArrayList()
    private val discovered: AtomicBoolean = AtomicBoolean(false)

    private fun discoverToolchains(scanSystem: Boolean) {
        if (discovered.compareAndExchange(false, true)) {
            return
        }
        discoverManagedToolchains()
        if (scanSystem) {
            discoverUnmanagedToolchains()
        }
    }

    private fun discoverManagedToolchains() {
        val cacheDir = parameters
            .gradleUserHome
            .asFile
            .get()
            .toPath()
            .resolve("caches")
            .resolve("com.falsepattern.zigbuild")
            .resolve("toolchains-1")

        if (!cacheDir.exists()) {
            return
        }

        Files.list(cacheDir).use { stream ->
            stream
                .filter { it.fileName.pathString.endsWith(".properties") }
                .forEach { f ->
                    val fileNameString = f.fileName.pathString
                    val name = fileNameString.substring(0, fileNameString.length - ".properties".length)
                    val info = f.readToolchainInfo() ?: return@forEach
                    val unpackDir = cacheDir.resolve(name)
                    if (!unpackDir.exists())
                        return@forEach
                    val toolchainDirectory = determineStructure(unpackDir)
                    val fileProperty = objectFactory.fileProperty()
                    fileProperty.set(toolchainDirectory)
                    val toolchain = fileProperty.asFile.map(objectFactory.newInstance<ToolchainCreatingTransformer>(info))
                    val resolvedToolchain = ResolvedZigToolchain(info, toolchain)
                    managedToolchains.add(resolvedToolchain)
                }
        }
    }

    private fun discoverUnmanagedToolchains() {
        val unmanagedSequence = detectUnmanagedToolchains {
            providerFactory.exec(it)
        };

        unmanagedSequence.forEach { (version, path) ->
            val info = ResolvedZigToolchainInfo(version, ZigOperatingSystemTarget.current, ZigArchitectureTarget.current)
            val fileProperty = objectFactory.fileProperty()
            fileProperty.set(path.toFile())
            val toolchain = fileProperty.asFile.map(objectFactory.newInstance<ToolchainCreatingTransformer>(info))
            val resolvedToolchain = ResolvedZigToolchain(info, toolchain)
            unmanagedToolchains.add(resolvedToolchain)
        }
    }

    fun forInfo(info: ResolvedZigToolchainInfo, project: Project): Provider<ZigToolchain> {
        // TODO: this still fails conf cache on the first time downloading a toolchain -- this may be unavoidable without bypassing gradle's dependency downloading
        val property = objectFactory.property<ResolvedZigToolchainInfo>()
        property.set(info)
        return property.map(project.objects.newInstance<ServiceTransformer>(project.extensions.getByType<ZigExtension>().scanSystem))
    }

    fun toolchainFor(spec: ZigToolchainSpec, project: Project, providerInfos: Provider<List<ZigToolchainProviderInfo>>, scanSystem: Boolean): Provider<ZigToolchain> {
        discoverToolchains(scanSystem)

        for (existing in managedToolchains) {
            if (existing.info.matches(spec) && existing.info.os == ZigOperatingSystemTarget.current && existing.info.arch == ZigArchitectureTarget.current) {
                return forInfo(existing.info, project)
            }
        }

        for (providerInfo in providerInfos.get()) {
            val name = providerInfo.info.name
            val provider = providerInfo.provider.get()

            val result = provider.resolve(object: ZigToolchainRequest {
                override val zigToolchainSpec get() = spec
                override val os get() = ZigOperatingSystemTarget.current
                override val arch get() = ZigArchitectureTarget.current
            })

            if (!result.isPresent) {
                continue
            }

            val toolchainDownload = result.get()

            val uri = toolchainDownload.uri.toString()
            val rootUri = providerInfo.info.rootUri?.toString() ?: continue
            if (!uri.startsWith(rootUri)) {
                throw IllegalStateException("Toolchain URI $uri does not start with root URI $rootUri")
            }

            var rest = uri.substring(rootUri.length)
            if (rest.startsWith("/")) {
                rest = rest.substring(1)
            }

            val dep = project.dependencies.create("$ZIG_TOOLCHAIN_PROVIDER_SERVICE_PREFIX.$name:zig:$rest")

            val resolvedSpec = ResolvedZigToolchainInfo(toolchainDownload.version, ZigOperatingSystemTarget.current, ZigArchitectureTarget.current)

            val config = project.configurations.detachedConfiguration(dep)

            val toolchainDirectory = config.incoming.artifactView{}.artifacts.resolvedArtifacts.map { set ->
                if (set.size != 1) {
                    throw IllegalStateException("Expected exactly one artifact, but got ${set.size}")
                }
                set.single().file
            }.map(project.objects.newInstance<ToolchainUnpackTransform>(resolvedSpec))

            val toolchainProvider = toolchainDirectory.map(objectFactory.newInstance<ToolchainCreatingTransformer>(resolvedSpec))
            managedToolchains.add(ResolvedZigToolchain(resolvedSpec, toolchainProvider))
            return forInfo(resolvedSpec, project)
        }

        for (existing in unmanagedToolchains) {
            if (existing.info.matches(spec) && existing.info.os == ZigOperatingSystemTarget.current && existing.info.arch == ZigArchitectureTarget.current) {
                return forInfo(existing.info, project)
            }
        }

        throw IllegalStateException("No zig toolchain provider found for version ${spec.version.get()}")
    }

    fun existingToolchain(info: ResolvedZigToolchainInfo, scanSystem: Boolean): ZigToolchain {
        discoverToolchains(scanSystem)
        for (existing in managedToolchains) {
            if (existing.info == info) {
                return existing.toolchain.get()
            }
        }
        for (existing in unmanagedToolchains) {
            if (existing.info == info) {
                return existing.toolchain.get()
            }
        }
        throw IllegalArgumentException("No toolchain matching $info")
    }

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:Inject
    abstract val archiveOperations: ArchiveOperations

    fun unpack(input: File, info: ResolvedZigToolchainInfo): File {
        val version = info.zigVersion
        val arch = info.arch
        val os = info.os

        val key = "$version-${arch.name}-${os.name}"

        val cacheDir = parameters.gradleUserHome.get().asFile.toPath().resolve("caches").resolve("com.falsepattern.zigbuild").resolve("toolchains-1")
        val outputDir = cacheDir.resolve(key)
        val lockFile = cacheDir.resolve("$key.lock")
        val existsFile = cacheDir.resolve("$key.properties")
        lock(lockFile).use { _ ->
            if (existsFile.exists()) {
                val existingInfo = existsFile.readToolchainInfo()
                if (existingInfo == info) {
                    return determineStructure(outputDir)
                }
            }

            if (Files.exists(outputDir)) {
                FileUtils.deleteDirectory(outputDir.toFile())
            }


            val ext = if (os == ZigOperatingSystemTarget.WINDOWS) "zip" else "tar.xz"
            decompress(input, outputDir.toFile(), ext)
            existsFile.writeToolchainInfo(info)
            return determineStructure(outputDir)
        }
    }

    private fun decompress(input: File, outputDir: File, extension: String) {
        when(extension) {
            "tar.xz" -> {
                val tmpFile = Files.createTempFile("zigbuild", ".tar")
                decompressXZ(input, tmpFile)
                unpackTar(tmpFile.toFile(), outputDir)
                Files.delete(tmpFile)
            }
            "zip" -> unpackZip(input, outputDir)
            else -> throw IllegalArgumentException("Cannot unpack file ${input.absolutePath}, unsupported extension $extension")
        }
    }

    private fun decompressXZ(input: File, output: Path) {
        input.inputStream().buffered().let { XZCompressorInputStream(it) }.use {
            Files.copy(it, output, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun unpackTar(input: File, outputDir: File) {
        fileSystemOperations.copy {
            from(archiveOperations.tarTree(input))
            into(outputDir)
        }
    }

    private fun unpackZip(input: File, outputDir: File) {
        fileSystemOperations.copy {
            from(archiveOperations.zipTree(input))
            into(outputDir)
        }
    }

    fun determineStructure(outputDir: Path): File {
        return tryDetermineStructure(outputDir)?.toFile() ?: throw IllegalStateException("No directories found in toolchain directory $outputDir")
    }

    private fun lock(lockFile: Path): Lock {
        Files.createDirectories(lockFile.parent)
        LOGGER.debug("Acquiring lock at {}", lockFile)

        // Try 5 times to get a file channel -- this doesn't block anything yet
        var channel: FileChannel? = null
        var last: IOException? = null
        for (attempt in 0 until 5) {
            try {
                channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                break
            } catch (e: AccessDeniedException) {
                last = e
                //Wait one second, try again
                Thread.sleep(1000)
            } catch (e: IOException) {
                last = e
                break
            }
        }
        if (channel == null) {
            throw IOException("Failed to create lock-file $lockFile", last)
        }

        // Now we try to get a lock on the file which will block other precesses
        var fileLock: FileLock?
        var startTime = System.currentTimeMillis()
        while (true) {
            try {
                fileLock = channel.tryLock()
                if (fileLock != null) {
                    break
                }
            } catch(_: OverlappingFileLockException) {
                // The lock is held by this process already, in another thread
            } catch (e: IOException) {
                try {
                    channel.close()
                } catch(_: IOException) {}
                throw IOException("Error while trying to acquire lock on $lockFile", e)
            }

            if (System.currentTimeMillis() - startTime > 1000 * 60 * 5) {
                // If we've waited more than 5 minutes, fail
                throw RuntimeException("Failed to acquire lock on $lockFile; timed out after 5 minutes")
            }
            Thread.sleep(1000)
        }

        @Suppress("SENSELESS_COMPARISON") //Needed because gradle uses an older kotlin compiler
        if (fileLock == null)
            throw AssertionError()

        LOGGER.debug("Acquired lock at {}", lockFile)

        return Lock(fileLock, lockFile)
    }

    companion object {
        const val SERVICE_NAME = "com.falsepattern.zigbuild.internal.toolchain.unpackingService"
        const val ZIG_TOOLCHAIN_PROVIDER_SERVICE_PREFIX = "com.falsepattern.zigbuild.internal.toolchain.providers"
        private val LOGGER = LoggerFactory.getLogger(ToolchainUnpackingService::class.java)
    }

    interface Parameters: BuildServiceParameters {
        val gradleUserHome: DirectoryProperty
    }

    abstract class ToolchainCreatingTransformer @Inject constructor(private val spec: ResolvedZigToolchainInfo): Transformer<ZigToolchain, File> {
        @get:Inject
        protected abstract val objectFactory: ObjectFactory

        override fun transform(file: File): ZigToolchain {
            val dirProperty = objectFactory.directoryProperty()
            dirProperty.set(file)
            return objectFactory.newInstance<ZigToolchain>(dirProperty.get(), spec)
        }
    }

    abstract class ServiceTransformer @Inject constructor(private val scanSystem: Boolean): Transformer<ZigToolchain, ResolvedZigToolchainInfo> {
        @get:ServiceReference(SERVICE_NAME)
        abstract val service: Property<ToolchainUnpackingService>

        override fun transform(info: ResolvedZigToolchainInfo): ZigToolchain {
            return service.get().existingToolchain(info, scanSystem)
        }
    }

    private data class Lock(private val fileLock: FileLock, private val lockFile: Path): AutoCloseable {
        override fun close() {
            LOGGER.debug("Releasing lock on {}", lockFile)
            try {
                fileLock.release()
            } catch (e: IOException) {
                LOGGER.error("Failed to release lock on {}", fileLock.channel().toString(), e)
            }
            try {
                fileLock.channel().close()
            } catch (_: IOException) {}
        }
    }
}


private fun tryDetermineStructure(outputDir: Path): Path? {
    //More efficient than reading the whole list
    val filesInDirectory = runCatching { Files.newDirectoryStream(outputDir).use { it.take(2) } }.getOrNull()
    if (filesInDirectory == null || filesInDirectory.isEmpty()) {
        return null
    }

    // If there's one file, we go down one level. Otherwise, zig is here
    return if (filesInDirectory.size > 1) {
        outputDir
    } else {
        filesInDirectory[0]
    }
}

private fun detectUnmanagedToolchains(executor: (ExecSpec.() -> Unit) -> ExecOutput): Sequence<Pair<ZigVersion, Path>> {
    val exeName = if (ZigOperatingSystemTarget.current == ZigOperatingSystemTarget.WINDOWS) {
        "zig.exe"
    } else {
        "zig"
    }
    val pathToolchains = findAllExecutablesOnPATH(exeName)
    val wellKnown = wellKnown.asSequence().flatMap { dir ->
        runCatching<Sequence<Path>> {
            Files.newDirectoryStream(dir).use { stream ->
                stream.toList().asSequence()
                    .mapNotNull { path ->
                        if (path == null)
                            return@mapNotNull null

                        val zigDir = tryDetermineStructure(path) ?: return@mapNotNull null

                        val exe = zigDir.resolve(exeName)
                        if (!exe.isRegularFile() || !exe.isExecutable()) {
                            return@mapNotNull null
                        }
                        return@mapNotNull exe
                    }
            }
        }.getOrElse { emptySequence() }
    }
    val joined = sequenceOf(pathToolchains, wellKnown).flatten()
    return joined.mapNotNull { path ->
        runCatching {
            val version = executor {
                commandLine(path.absolutePathString(), "version")
            }.standardOutput.asText.get().trim()
            if (version.isBlank()) {
                return@mapNotNull null
            }
            ZigVersion.of(version) to path.parent
        }.getOrNull()
    }
}

private fun findAllExecutablesOnPATH(exe: String) = sequence {
    val paths = System.getenv("PATH")?.split(File.pathSeparatorChar) ?: return@sequence
    for (dir in paths) {
        val path = dir.toNioPathOrNull()?.absolute() ?: continue
        if (!path.toFile().exists() || !path.isDirectory())
            continue
        val exePath = path.resolve(exe).absolute()
        if (!exePath.isRegularFile() || !exePath.isExecutable())
            continue
        yield(exePath)
    }
}

private val homePath: Path? by lazy {
    System.getProperty("user.home")?.toNioPathOrNull()?.takeIf { it.isDirectory() }
}

private val xdgDataHome: Path? by lazy {
    System.getenv("XDG_DATA_HOME")?.toNioPathOrNull()?.takeIf { it.isDirectory() } ?: when(ZigOperatingSystemTarget.current) {
        ZigOperatingSystemTarget.MACOS -> homePath?.resolve("Library")
        ZigOperatingSystemTarget.WINDOWS -> System.getenv("LOCALAPPDATA")?.toNioPathOrNull()
        else -> homePath?.resolve(Path.of(".local", "share"))
    }?.takeIf { it.isDirectory() }
}

/**
 * Returns the paths to the following list of folders:
 *
 * 1. DATA/zig
 * 2. DATA/zigup
 * 3. HOME/.zig
 *
 * Where DATA is:
 *  - ~/Library on macOS
 *  - %LOCALAPPDATA% on Windows
 *  - $XDG_DATA_HOME (or ~/.local/share if not set) on other OSes
 *
 * and HOME is the user home path
 */
private val wellKnown: List<Path> by lazy {
    val res = ArrayList<Path>()
    xdgDataHome?.let {
        res.add(it.resolve("zig"))
        res.add(it.resolve("zigup"))
    }
    homePath?.let { res.add(it.resolve(".zig")) }
    res
}

private fun String.toNioPathOrNull(): Path? = runCatching {
    Paths.get(this)
}.getOrNull()