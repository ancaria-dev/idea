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

This IntelliJ IDEA plugin is published on the JetBrains Marketplace as **Sacred
Mod Development**. It adds a New Project wizard, a run configuration, two gutter
icons and one settings page for Sacred Gold mod development.

The plugin doesn’t define the mod format. Its wizard uses the same scaffolder as
`coderpack new`. The run configuration calls the project’s Gradle wrapper and a
released Sacred Mod Loader executable. The gutter icons read the API published
by `coderpack`. Templates live in the `build` repository, with no duplicate copy
here to drift out of sync.

## What it does

### File | New | Project | Sacred Mod

The wizard asks for a group, mod name, derived mod ID, description, template,
mod language and build script DSL. **Create a Git repository** initializes a
repository, adds the project URL as `origin` and leaves the history without an
initial commit. **Create an SRML mod repository** adds `registry.toml` and a
workflow that publishes each new version for launcher installation. Both
checkboxes are selected by default. Enter the project’s address in
**Repository URL**. Author, package and mod version are under **Advanced**.

Language and build script are separate choices:

- **Language** selects Java, Kotlin or Groovy for the mod source. Kotlin and
  Groovy add their runtime libraries to the JAR. Java needs no separate
  runtime.
- **Build script** selects Kotlin DSL or Groovy DSL for the Gradle build,
  producing `build.gradle.kts` or `build.gradle`. The source language does not
  constrain this choice.

The scaffolder supplies the template, language and build DSL lists. Rebuilding
the plugin after a template change in `build` updates the wizard without
changing its source. Each template shows only the languages it supports.

The wizard passes the same values to the scaffolder as `coderpack new`, so both
routes produce the same project layout. Generated projects build and pass the
scaffolder’s checks without manual fixes.

### Run Sacred

The plugin adds **Run Sacred** when it finds `dev.ancaria.coderpack` in the root
build script or one immediate child. It does not create a duplicate
configuration. If no Gradle project is linked yet, it links the root project.

By default, Run Sacred executes
`gradlew installSacredMod -PsacredDir=<game folder> --console=plain`, installs
the selected Sacred Mod Loader release in the game folder and starts it.
Windows uses `gradlew.bat`. If the selected release is missing from the local
cache, the plugin downloads it first. Before copying the executable, it
compares the installed and cached files by SHA-256.

If the game folder is not configured, Run opens the plugin’s settings page.

With **Build and install the mod first** selected, the Run console follows
Gradle and shows its output. Sacred Mod Loader starts as a detached process
after a successful build, so Stop does not end the game. If the checkbox is
cleared or the project has no Gradle wrapper, the console follows the loader
process and Stop ends it. **Show the loader console** adds `--debug`.

The run configuration stores only its two checkboxes. The game folder and loader
release are application-level settings, so shared run configurations do not
contain machine-specific absolute paths.

### Gutter icons

The plugin adds gutter icons beside `SacredMod` implementations and
`@Subscribe` methods that listen for game events. One UAST-based implementation
supports Java, Kotlin and Groovy.

Entrypoint detection uses both PSI and the build script. PSI identifies every
concrete, nonabstract class that implements `SacredMod`, while the `entrypoint`
value in the build script identifies the class the loader will instantiate.
Each candidate gets an icon, and its tooltip reports whether it matches the
configured entrypoint. Clicking it opens the `entrypoint` declaration when the
plugin can find one.

An event icon appears on a method with `@Subscribe`, exactly one parameter and a
parameter type from the Sacred event hierarchy. Before the first Gradle sync,
classes in the event package are also recognized, except for `Guard`. The
tooltip names the event and any nondefault listener priority. Clicking the icon
opens the event class.

### Settings | Tools | Sacred Mod Development

This application-level settings page stores the Sacred Gold folder and Sacred
Mod Loader release. The selected folder must contain `pureHD.exe`, `Sacred.exe`
or `Game.exe`.

The plugin reads stable releases from GitHub. It excludes drafts, prereleases
and entries without a `Sacred Mod Loader.exe` asset. Cached versions remain
available while the request runs, and **Refresh** requests the list again. An
empty value means the latest release. Once GitHub returns the list, it appears
as `Latest (<version>)`. If GitHub is unavailable, the plugin uses the newest
cached version. The selection is stored for the whole IDE. Apply downloads the
release when it is not cached yet.

Downloads are stored at
`<gradle user home>/caches/ancaria/launcher/<version>/Sacred Mod Loader.exe`.
The plugin does not delete older versions automatically.

## Install

From the Marketplace, open **Settings | Plugins | Marketplace** and search for
“Sacred Mod Development”.

To install from a file, download `sacred-idea-<version>.zip` from this
repository’s releases or use the local
`build/distributions/sacred-idea-<version>.zip`, then open
**Settings | Plugins | ⚙ | Install Plugin from Disk**.

The plugin supports IntelliJ IDEA 2025.2–2025.3, Community or Ultimate.

## Build

You need a JDK on `PATH`. The Gradle toolchain resolver downloads Temurin 21 if
JDK 21 is unavailable, because the plugin targets the JVM used by the supported
IDE versions.

```
./gradlew build          # compile, test, build the zip
./gradlew runIde         # a sandbox IDE with the plugin in it
./gradlew verifyPlugin   # what the marketplace runs on upload
```

The first build downloads an IntelliJ IDEA distribution of about one gigabyte
to the Gradle cache.

If the `build` repository is checked out beside `idea`, Gradle includes
`../build/gradle` as a composite build. Template changes then appear in the
wizard without an intermediate publication. Otherwise,
`dev.ancaria.coderpack:templates` resolves from Maven Local or Maven Central. CI
publishes the scaffolder to Maven Local and tests that repository-based path.

## Release

CI uses `pluginVersion` in `gradle.properties` as the release gate. On `master`,
a missing `v<version>` tag causes the plugin to be uploaded to the JetBrains
Marketplace and published as a GitHub release with the same ZIP. If the tag
already exists, CI builds and verifies the plugin without publishing it again.

Publishing needs one secret, `PUBLISH_TOKEN`. The plugin is not signed with a
certificate of its own: the Marketplace signs it, and self-signing is a door
that does not open the other way.

## License

MIT. See [LICENSE](LICENSE). The plugin does not redistribute game files and
requires your own copy of Sacred Gold.
