# idea

Workspace rules and release chain: see `../CLAUDE.md`.

## Boundaries

- This is the IntelliJ IDEA plugin "Sacred Mod Development", id `dev.ancaria.idea`. Never change the id; it is the Marketplace identity and a new id has no upgrade path.
- Never redefine another repository's contract. The wizard calls `build`'s scaffolder, Run Sacred calls the project's Gradle wrapper and a released launcher, markers read the `coderpack` API.
- Name everything shared with another repository once, in `Sacred.kt`: API types, launcher repository and asset, install task and property, paths, game executables.
- `dev.ancaria.coderpack:templates` resolves from Maven Central. `mavenLocal()` comes first in `build.gradle.kts`, so `publishToMavenLocal` in `build` overrides it for an unreleased template change.
- Keep `exclude(group = "org.jetbrains.kotlin")` on the `templates` dependency. A second Kotlin stdlib on the plugin classpath causes `NoClassDefFoundError`.

## Wizard

- Never copy templates, languages, or DSLs here. `Catalog` reads them from `templates`. Rebuild language choices when the template changes, and never offer a language the template lacks.
- `Scaffolding` builds the same value map as `coderpack new`, then calls `Scaffold.plan` and `Scaffold.write`. It writes with `force = true` because the IDE already created the directory; preserve unrelated files.
- `Git.init` adds `origin` and never commits.
- Keep Gradle linking out of `SacredProjectSetup`, which only refreshes the VFS. `StartupManager.runAfterOpened` is internal API and fails verification, so `SacredRunSetup` links as a post-startup activity.

## Settings

- The game folder and launcher release are application-level settings (`sacred.xml`). Never move them into project state or a run configuration; run configurations get committed and shared.
- A game folder is valid with `pureHD.exe`, `Sacred.exe`, or `Game.exe`, case-insensitive, in that order.
- Releases come unauthenticated from `https://api.github.com/repos/ancaria-dev/launcher/releases?per_page=50`, cached per IDE session. Exclude drafts, prereleases, and releases without `Sacred Mod Loader.exe`.
- An empty version means latest stable. If the lookup fails, latest falls back to the newest cached version. There is no third state.
- `LauncherInstall.provide` caches at `<Gradle user home>/caches/ancaria/launcher/<version>/Sacred Mod Loader.exe`, via a `.part` file and an atomic move. It never deletes old releases.

## Run Sacred

- `SacredRunConfiguration` stores only `installMod` and `showConsole`.
- On Run: fetch the selected launcher if missing; replace `<game>/Sacred Mod Loader.exe` when its SHA-256 differs; if `installMod` and a wrapper exist, run `gradlew installSacredMod -PsacredDir=<game> --console=plain`; start the launcher only if the build succeeded.
- Keep the task name unqualified so multi-mod layouts install every mod.
- With a build, the console follows Gradle and the launcher starts detached. Without one, the console follows the launcher and Stop ends it. `showConsole` adds `--debug`.
- `SacredRunSetup` acts only when the root or an immediate child build script contains `dev.ancaria.coderpack`. It links only when no Gradle project is linked, creates a configuration only when none exists, and selects it only when nothing else is selected.

## Gutter markers

- Keep Kotlin and Groovy registrations in the optional descriptors `sacred-kotlin.xml` and `sacred-groovy.xml`. A hard language-plugin dependency blocks installation without that plugin.
- Register markers on leaves: convert the parent to a `UDeclaration` and answer only when the leaf is its `uastAnchor`. Otherwise IntelliJ logs bad anchors or the icon duplicates.
- `EntrypointMarker` accepts concrete classes extending the abstract `dev.ancaria.coderpack.api.SacredMod`, directly or through abstract classes. `ModDescriptor` reads `entrypoint = "..."` or `entrypoint.set("...")` from the build script.
- `EventMarker` needs `@Subscribe`, one parameter, and an event type. Its pre-sync package fallback follows the linter's definition of an event: see `../build/CLAUDE.md`.
- Markers ignore the return type. Checking the returned `Mutation` is the linter's job.

## Build

- `./gradlew build`, `runIde`, `verifyPlugin`, `buildPlugin`.
- Unlike the other repositories, this build uses a toolchain: the Foojay resolver downloads Temurin 21 if missing.
- The first build downloads an IntelliJ IDEA distribution of about 1 GB. `verifyPlugin` may download one per checked IDE.
- Tests are JUnit 5 and start no IDE. The end-to-end test of generated projects lives in `build`.
- Keep JUnit 4 on the test runtime. IDE services reference it; without it the executor fails with `org/junit/runners/model/Statement`.
- Keep `buildSearchableOptions = false`; enabling it starts a headless IDE.
- Keep `languageVersion` and `apiVersion` at the Kotlin version of the oldest supported IDE. Keep `kotlin.stdlib.default.dependency=false`.
- Keep the deprecated `linkAndRefreshGradleProject` until the oldest supported IDE has its suspend replacement.
- `verifyPlugin` may report `<supportsKotlinPluginMode>` missing. Keep the K1/K2 declaration in `sacred-kotlin.xml`; in the main descriptor it would reference an unknown extension point without the Kotlin plugin.
- The plugin icon is four PNGs in `META-INF/` (`pluginIcon.png`, `pluginIcon@2x.png`, and `_dark` copies). No SVG. Replace all four together.

## Release

- Raise `pluginVersion` only to publish. A missing `v<pluginVersion>` tag publishes the same ZIP to the Marketplace and a GitHub release.
- `tools/version.ps1` leaves `CHANGELOG.md` (needs a new heading) and the `coderpack` entry in `gradle/libs.versions.toml` (tracks `build`) alone.
- Publishing needs one secret, `PUBLISH_TOKEN`.
- Never set `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, or `PRIVATE_KEY_PASSWORD` unless I decide so. The Marketplace signs uploads, and once an account uploads a self-signed plugin every later upload must be signed.
