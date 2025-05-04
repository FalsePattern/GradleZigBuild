import com.falsepattern.zigbuild.tasks.ZigBuildTask
import com.falsepattern.zigbuild.tasks.ZigTask
import com.falsepattern.zigbuild.toolchain.ZigVersion

plugins {
    id("com.falsepattern.zigbuild")
}

zig {
    toolchain {
        version = ZigVersion.of("0.14.0")
    }
}

tasks {
    register<ZigTask>("zigEnv").configure {
        options {
            compilerArgs.add("env")
            env.put("NO_COLOR", "1")
        }
    }

    val cache = project.layout.buildDirectory.dir("zig-cache-shared")
    val sources = listOf("build.zig", "build.zig.zon", "src/zig")
    val prefix = project.layout.buildDirectory.dir("zig-build")

    register<ZigBuildTask>("zigBuildInstall").configure {
        options {
            zigCache = cache
        }
        sourceFiles.from(sources)
        prefixDirectory = prefix
    }

    register<ZigBuildTask>("zigBuildTest").configure {
        options {
            zigCache = cache
            steps.add("test")
        }
        sourceFiles.from(sources)
        prefixDirectory = prefix
        outputs.upToDateWhen { false }
    }
    register<ZigBuildTask>("zigBuildRun").configure {
        options {
            zigCache = cache
            steps.add("run")
        }
        sourceFiles.from(sources)
        prefixDirectory = prefix
        outputs.upToDateWhen { false }
    }

    register<Exec>("zigEnvExec").configure {
        executable = zig.compilerFor{ version = ZigVersion.of("0.13.0") }.get().executablePath.asFile.absolutePath
        setArgs(listOf("env"))
    }
}