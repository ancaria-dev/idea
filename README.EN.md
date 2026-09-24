<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ%20Platform-2.18.1-000000?style=for-the-badge&logo=intellijidea&logoColor=white)](https://github.com/JetBrains/intellij-platform-gradle-plugin)
[![IDEA](https://img.shields.io/badge/IDEA-2025.2%E2%80%932025.3-FE315D?style=for-the-badge&logo=intellijidea&logoColor=white)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-4B5563?style=for-the-badge)](LICENSE)
[![Sacred](https://img.shields.io/badge/Sacred-Community-8B1A1A?style=for-the-badge&labelColor=1C1410)](https://ancaria.dev)

[Русский](README.md)

</div>

# Sacred Mod Development

An IntelliJ IDEA plugin that lets you create and run a Sacred Gold mod without
leaving the IDE.

A wizard creates a ready-to-build Gradle project for your mod. The Run Sacred
button builds the mod, installs it in the game, and starts the launcher.
Gutter icons mark your mod's entry point and its event listeners.

The plugin keeps no templates of its own. The wizard reads them from the same
scaffolder as `coderpack new`, so both give you the same project.

## Getting started

1. Install the plugin: open **Settings | Plugins | Marketplace** and search for
   **Sacred Mod Development**, or open its
   [Marketplace page](https://plugins.jetbrains.com/plugin/34165-sacred-mod-development).
2. Create a project: **File | New | Project | Sacred Mod**.
3. Set the game folder: **Settings | Tools | Sacred Mod Development**.
4. Press **Run Sacred**.

The plugin supports IntelliJ IDEA 2025.2 and 2025.3, Community and Ultimate.
To install it from a file, download `sacred-idea-<version>.zip` from the
releases and use **Settings | Plugins | ⚙ | Install Plugin from Disk**.

## Features

### New Project wizard

The wizard asks for a group, mod name, description, template, mod language,
and build script language. It derives the mod ID from the name. Author,
package, and version sit under **Advanced**.

The mod language and the build language are separate choices:

- **Language** is the mod's language: Java, Kotlin, or Groovy. Kotlin and
  Groovy pack their runtime into the mod jar. Java needs none.
- **Build script** is `build.gradle.kts` or `build.gradle`.

Two checkboxes are on by default. **Create a Git repository** creates a
repository and adds the **Repository URL** as `origin`, without committing
anything. **Create an SRML mod repository** adds `registry.toml` and a
workflow that releases each new version of the mod. Players can then install
it from the launcher.

Templates and languages come from the scaffolder in `build`. A new template
shows up in the wizard once the plugin is rebuilt, and each template offers
only the languages it supports.

### Run Sacred

The plugin adds a **Run Sacred** configuration on its own when it finds
`dev.ancaria.coderpack` in the root build script or one folder below. If the
project isn't linked to Gradle yet, the plugin links it.

When you press it, Run Sacred:

1. Builds the mod and installs it in the game with
   `gradlew installSacredMod -PsacredDir=<game folder> --console=plain`.
2. Downloads the selected Sacred Mod Loader release if it isn't cached.
3. Copies the launcher into the game folder if its SHA-256 differs from the
   cached one.
4. Starts the launcher.

If no game folder is set, Run Sacred opens the settings page.

The configuration has two checkboxes. **Build and install the mod first**
turns the build on: the console shows Gradle output, and the launcher starts
on its own, so Stop doesn't close the game. Without the build, or without a
Gradle wrapper, the console follows the launcher, and Stop ends it. **Show the
loader console** adds `--debug`.

The game folder and launcher version live in the IDE settings, not in the
configuration. A shared configuration file therefore holds no paths from your
machine.

### Gutter icons

An entry point icon appears on every non-abstract class that extends
`SacredMod`, directly or through an abstract class of your own. Its tooltip
says whether the class matches the `entrypoint` in the build script. Clicking
it opens that declaration.

An event icon appears on a method with `@Subscribe` and a single event
parameter. The tooltip names the event and any non-default priority. A
`MONITOR` listener gets a grey icon. Clicking it opens the event class. The
icon doesn't check the return type: the linter does that at build time.

The icons work in Java, Kotlin, and Groovy.

### Settings

**Settings | Tools | Sacred Mod Development** stores the Sacred Gold folder
and the Sacred Mod Loader release. The folder must contain `pureHD.exe`,
`Sacred.exe`, or `Game.exe`.

The plugin reads the release list from GitHub and shows only stable releases
that carry `Sacred Mod Loader.exe`. Cached versions stay available while the
list loads. **Refresh** asks for it again. An empty value means the latest
release and shows as `Latest (<version>)` once the list arrives. Without
GitHub, the plugin uses the newest cached version. **Apply** downloads the
selected release.

Launchers are stored in
`<gradle user home>/caches/ancaria/launcher/<version>/Sacred Mod Loader.exe`.
The plugin doesn't delete old versions.

## Building

Gradle needs a JDK on `PATH`. The plugin targets Java 21, and Gradle downloads
Temurin 21 if it's missing.

```
./gradlew build          # compile, test, and build the zip
./gradlew runIde         # a sandbox IDE with the plugin
./gradlew verifyPlugin   # the same check the Marketplace runs
```

The first build downloads an IDE distribution of about one gigabyte into the
Gradle cache.

The templates come from Maven Central as `dev.ancaria.coderpack:templates`. To
try an unreleased template, run `publishToMavenLocal` in the `build`
repository: Maven Local comes first.

## Releases

The plugin version is `pluginVersion` in `gradle.properties`. When `master`
has a version with no `v<version>` tag yet, CI publishes the plugin to the
JetBrains Marketplace and creates a GitHub release with the same zip. If the
tag exists, CI only builds and verifies the plugin.

Publishing needs one secret, `PUBLISH_TOKEN`. The plugin isn't signed with a
certificate of its own: the Marketplace signs it. Once you start self-signing,
there's no going back.

## License

MIT, see [LICENSE](LICENSE). The plugin contains no game files. You need your
own copy of Sacred Gold.
