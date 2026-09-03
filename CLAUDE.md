# IDEA plugin

## Repository purpose

This repository contains the IntelliJ IDEA plugin published on the JetBrains
Marketplace as **Sacred Mod Development**. It adds a New Project wizard, a Run
Sacred configuration, two gutter markers, and an application settings page.
The plugin is written in Kotlin and built with the IntelliJ Platform Gradle
Plugin.

The plugin presents tools owned by other repositories. It must not redefine
their contracts. The wizard calls the scaffolder from `build`. The run
configuration calls the project's Gradle wrapper and a released launcher
executable. The gutter markers inspect the API published by `coderpack`.

## Repository layout

| Path | Contents |
|---|---|
| `src/main/kotlin/dev/ancaria/idea/` | `Sacred.kt`, which holds names shared with other repositories, plus `SacredIcons.kt`, `SacredBundle.kt`, and `Notices.kt`. |
| `.../wizard/` | `SacredProjectWizard`, `SacredModStep`, `Catalog`, `Scaffolding`, and `SacredProjectSetup`. |
| `.../settings/` | Application-level `SacredSettings` and `SacredState`, the `SacredConfigurable` page, and `GameFolder` validation. State is stored in `sacred.xml`. |
| `.../launcher/` | `LauncherReleases`, `LauncherCache`, and `LauncherInstall`. |
| `.../run/` | The Run Sacred configuration type, options, editor, state, command construction, and startup setup. |
| `.../markers/` | `SacredMarker`, `EntrypointMarker`, `EventMarker`, and `ModDescriptor`. |
| `src/main/resources/META-INF/plugin.xml` | Marketplace metadata, dependencies, and core extension registrations. The build patches only `<version>` and `<idea-version>`. |
| `src/main/resources/META-INF/sacred-kotlin.xml` | Kotlin marker registrations and K1/K2 compatibility declaration. Loaded only when the Kotlin plugin is present. |
| `src/main/resources/META-INF/sacred-groovy.xml` | Groovy marker registrations. Loaded only when the Groovy plugin is present. |
| `src/main/resources/icons/` | Marker SVGs and `sacred_icon.png`, the game mark used as their drawing reference. |
| `src/main/resources/messages/SacredBundle.properties` | All user-facing strings. |
| `gradle.properties` | `pluginVersion`, `platformVersion`, `sinceBuild`, `untilBuild`, and Gradle settings. |

## New Project wizard

`SacredProjectWizard` implements `GeneratorNewProjectWizard` and is registered
at `com.intellij.newProjectWizard.generator`. The platform's
`NewProjectWizardBaseStep` supplies the project name and location.
`SacredModStep` adds the mod fields on the same page through `nextStep`.

The form collects the group, mod name, mod ID, description, template, source
language, build-script DSL, repository URL, package, author, and mod version.
It also controls Git initialization and SRML repository generation. The two
checkboxes default to enabled. The package, author, and version fields are
under Advanced.

Defaults flow from project name to mod name to mod ID, then from group and mod
ID to package. Each derived field stops tracking its source after the user
edits it. This is the default `GraphProperty.dependsOn` behavior.

**Language** selects the mod source language. **Build script** selects
`build.gradle.kts` or `build.gradle`. These choices are independent. A Java
mod can use either Gradle DSL, as can a Kotlin or Groovy mod.

`Catalog` reads templates, each template's supported languages, DSLs, and their
descriptions from `dev.ancaria.coderpack:templates`. Do not maintain parallel
lists here. Rebuild the language choices when the template changes, and never
offer a language that the selected template does not support. A template change
in `build` reaches the wizard when this plugin is rebuilt against that
scaffolder.

`Scaffolding` creates the same value map used by `coderpack new`, then calls
`Scaffold.plan` and `Scaffold.write`. Planning finishes before the first file
is written. The IDE has already created the project directory and `.idea`, so
the wizard writes with `force = true` and must preserve unrelated files.
When Git is enabled, `Git.init` initializes the repository and adds the
resolved project URL as `origin`. It does not create a commit.

`SacredProjectSetup` refreshes the VFS synchronously after writing files. Keep
Gradle linking out of this class. Linking must happen after the project window
opens, while `StartupManager.runAfterOpened` is internal API and fails plugin
verification. `SacredRunSetup` handles linking as a post-startup activity for
both generated and cloned projects.

## Settings

**Settings | Tools | Sacred Mod Development** stores the Sacred Gold folder
and launcher release at application level. Do not move either value into
project state. Several mod projects on one machine share one game installation
and one launcher selection.

The game folder is valid when it contains `pureHD.exe`, `Sacred.exe`, or
`Game.exe`, matched without case sensitivity and checked in that order. The
address table belongs to `pureHD.exe`, so that executable keeps priority.

Launcher releases come from
`https://api.github.com/repos/ancaria-dev/launcher/releases?per_page=50`.
The request is unauthenticated because the repository is public. Results are
cached for the IDE session. Drafts, prereleases, and releases without the
`Sacred Mod Loader.exe` asset are excluded.

The release selector starts with cached versions, then adds the GitHub response
on a pooled thread. Refresh forces another request. An empty version means the
latest stable release and appears as `Latest (<version>)` after a successful
lookup. If the lookup fails, latest falls back to the newest cached version.
There is no third selection state.

Applying settings calls `LauncherInstall.provide` under modal progress. Cached
executables are stored at
`<Gradle user home>/caches/ancaria/launcher/<version>/Sacred Mod Loader.exe`.
Downloads use a sibling `.part` file followed by an atomic move. The plugin
does not delete old releases.

## Run Sacred

`SacredRunConfiguration` stores only `installMod` and `showConsole`. Machine
paths and launcher versions belong in application settings because run
configurations can be committed and shared. If the game folder is missing or
invalid, configuration validation offers a direct settings-page fix. Pressing
Run also opens that page when needed.

Run provisioning follows this order:

1. Resolve and download the selected launcher release if it is absent.
2. Compare the cached and installed launcher executables by SHA-256.
3. Replace `<game>/Sacred Mod Loader.exe` when the bytes differ.
4. Build and install the mod when `installMod` is enabled and a Gradle wrapper exists.
5. Start the launcher.

The build command is:

```
gradlew installSacredMod -PsacredDir=<game> --console=plain
```

On Windows the wrapper is `gradlew.bat`. On other systems it is `gradlew`.
The task name stays unqualified so root projects and multi-mod SRML layouts
install all applicable mods.

When a build command exists, the Run console follows Gradle. A zero exit code
starts the launcher as a detached process. A failed build does not start it.
If mod installation is disabled, or if the project has no wrapper, there is no
build command. The console then follows the launcher process, and Stop can end
it. `showConsole` adds `--debug` to the launcher command.

`SacredRunSetup` runs for a project whose root or immediate child build script
contains `dev.ancaria.coderpack`. It links the root Gradle build only when no
Gradle project is already linked. It creates a Run Sacred configuration only
when none exists and selects it only when no other run configuration is
selected.

## Gutter markers

The Java, Kotlin, and Groovy registrations use the same UAST-based marker
classes. Keep Kotlin and Groovy registrations in optional descriptors. A hard
language-plugin dependency would prevent installation in an IDE without that
plugin.

`SacredMarker` must receive a leaf element, convert its parent to a
`UDeclaration`, and answer only when that leaf is `uastAnchor`. IntelliJ logs
incorrect anchors, and answering for every declaration token duplicates the
icon.

`EntrypointMarker` accepts only concrete, nonabstract classes that implement
`dev.ancaria.coderpack.api.SacredMod`. `ModDescriptor` reads
`entrypoint = "..."` or the older `entrypoint.set("...")` form from the
module's Gradle build script. The tooltip distinguishes a matching entrypoint,
another candidate, and a descriptor that could not be read. Clicking navigates
to the descriptor when available.

`EventMarker` requires `@Subscribe`, exactly one parameter, and an event type.
It checks inheritance from `dev.ancaria.coderpack.api.event.Event`, with the
event package as a pre-sync fallback and `Guard` excluded. The tooltip includes
a nondefault priority. Clicking navigates to the event class.

## Build and test

A JDK must be available to start Gradle. Compilation targets Java 21. The
Foojay toolchain resolver downloads Temurin 21 when that toolchain is missing.

```
./gradlew build          # compile, test, and build the plugin ZIP
./gradlew runIde         # start a sandbox IDE with the plugin
./gradlew verifyPlugin   # run Marketplace compatibility verification
./gradlew buildPlugin    # write build/distributions/sacred-idea-<version>.zip
```

The first build downloads an IntelliJ IDEA distribution of about one gigabyte
into the Gradle cache. `verifyPlugin` may download another distribution for
each IDE it checks.

The tests use JUnit 5 and do not start an IDE. `CatalogTest` compares wizard
choices with the scaffolder. `GameFolderTest` covers executable lookup and
paths. `ScaffoldingTest` writes projects to a temporary directory and checks
the wizard-to-scaffolder boundary. The `build` repository owns the end-to-end
test that compiles projects generated from the same templates.

## Relationship to `build`

This repository resolves `dev.ancaria.coderpack:templates`, which is published
by `build`. If `../build/gradle` exists, `settings.gradle.kts` includes it as a
composite build. Otherwise Gradle resolves the dependency from Maven Local or
Maven Central.

CI deliberately tests the repository path. It checks out `build` where the
composite lookup cannot find it, then runs:

```
./gradlew :templates:publishToMavenLocal :verify:publishToMavenLocal --no-daemon
```

Everything else shared with another repository is named once in `Sacred.kt`.
That includes API types, the launcher repository and asset, the installation
task and property, mod and launcher paths, and accepted game executables.

## Release process

`pluginVersion` in `gradle.properties` is the release gate. On `master`, CI
builds and verifies the plugin, then checks for `v<pluginVersion>`. A missing
tag causes the same ZIP to be published to the JetBrains Marketplace and a
GitHub release. An existing tag prevents both publication steps.

Publishing requires one secret, `PUBLISH_TOKEN`.

The `signing` block reads `CERTIFICATE_CHAIN`, `PRIVATE_KEY` and
`PRIVATE_KEY_PASSWORD`, and those are deliberately never set. The Marketplace
signs uploads itself, so the plugin needs no certificate of its own, and the
task is simply skipped when they are absent. Setting them is a one-way door:
once an account has uploaded a self-signed plugin, every later upload from it
has to be signed too. Leave them unset unless somebody decides otherwise on
purpose.

## Rules for coding agents

- Do not copy templates, languages, or build DSL definitions into this
  repository. `Catalog` must read them from the scaffolder.
- Do not store the game folder or launcher version in a run configuration.
- Do not add hard Kotlin or Groovy plugin dependencies for markers. Keep their
  registrations in optional descriptors.
- Register line markers on leaves and return a marker only for `uastAnchor`.
- Keep `languageVersion` and `apiVersion` at the Kotlin version bundled with
  the oldest supported IDE, regardless of the compiler used by this build.
- Keep `kotlin.stdlib.default.dependency=false`.
- Keep `exclude(group = "org.jetbrains.kotlin")` on the `templates`
  dependency. A second Kotlin standard library on the plugin classpath can
  cause `NoClassDefFoundError`.
- Do not change the plugin ID `dev.ancaria.idea`. The ID is its Marketplace
  identity. Changing it creates a separate plugin with no upgrade path for
  current users.
- Raise `pluginVersion` only when the change should publish a Marketplace
  update and GitHub release. No other change triggers a release.

## Warnings

- Keep `buildSearchableOptions = false`. Enabling it starts a headless IDE for
  settings indexing and makes the build much slower.
- `linkAndRefreshGradleProject` is deprecated, but its suspend replacement is
  unavailable in the oldest supported IDE. Keep the deprecated call until the
  minimum supported build provides the replacement.
- `verifyPlugin` fails on internal API use and warns on deprecated API use.
  This is why Gradle linking lives in the startup activity.
- The verifier may report `<supportsKotlinPluginMode>` missing because it does
  not inspect the optional descriptor where the declaration must live. Keep
  the K1 and K2 declaration in `sacred-kotlin.xml`. Moving it into the main
  descriptor would reference an unknown Kotlin extension point when the Kotlin
  plugin is absent. This plugin does not call the Kotlin Analysis API.
- Keep JUnit 4 on the test runtime even though all repository tests use JUnit
  5. Services from the IDE distribution reference JUnit 4. Without it, the
  executor fails with `org/junit/runners/model/Statement`.
- The Marketplace plugin icon is `META-INF/pluginIcon.svg`. It does not accept
  a PNG at that path. `icons/sacred_icon.png` is only a drawing reference.
