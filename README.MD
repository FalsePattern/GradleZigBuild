# Gradle ZigBuild

A gradle plugin that adds integration with zig toolchains, and preliminary integration with the zig build system.

## Setup

You can install the plugin by adding it in your build.gradle like so:

```kotlin
plugins {
    id("com.falsepattern.zigbuild") version "VERSION_GOES_HERE"
}
```

The plugin also ships with a default repository for automatically downloading zig toolchains (`https://ziglang.org/download/`), however, you will need to add the above lines to
your settings.gradle too.

If you don't add it to your settings.gradle, ZigBuild will look for zig toolchains in your PATH, and the following well-known locations:
- Windows:
  - `%LOCALAPPDATA%/zig`
  - `%LOCALAPPDATA%/zigup`
  - `%HOME%/.zig`
- Linux if XDG_DATA_HOME is set:
  - `$XDG_DATA_HOME/zig`
  - `$XDG_DATA_HOME/zigup`
  - `~/.zig`
- Linux if XDG_DATA_HOME is not set:
  - `~/.local/share/zig`
  - `~/.local/share/zigup`
  - `~/.zig`
- MacOS:
  - `~/Library/zig`
  - `~/Library/zigup`
  - `~/.zig`

This is identical to the default toolchain search/installation logic of [ZigBrains](https://github.com/falsepattern/zigbrains), so this effectively just integrates
toolchain management with it.

If you want to disable this autodetection, you can set `scanSystem` inside the `zig` project extension to false:
```kotlin
zig {
  scanSystem = false
}
```

Specific toolchains can also be acquired using `zig.compilerFor`. This compiler can be used as-is for `Exec` tasks, or
with some presets in the `ZigCompileTask` task.

For more information, you can check the `ZigBuild-test` subproject, which has a zig 0.14.0 example project hooked up to gradle.

## Special Thanks

A large part of this plugin is based on [ZigGradle](https://github.com/lukebemishprojects/ZigGradle) ported to kotlin, and heavily tweaked for better build.zig integration.
