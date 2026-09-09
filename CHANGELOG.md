# Changelog

## 0.100.2

- **A generated SRML repository keeps its own index.** The release workflow the
  wizard writes now regenerates `sacred.mods.repository.json` and commits it,
  so raising a mod version and pushing is the whole of publishing one. It
  indexes a mod that is already released from the jar on that release, and a
  version with no tag yet from the jar it is about to publish, so the checksum
  it advertises always describes the file behind the download URL. That is the
  one a launcher verifies before it installs anything. The old step compared
  the committed index against a freshly built jar, which fails whenever the jar
  is built on a different machine than the one that wrote the index.

## 0.100.1

- **Kotlin projects from the wizard now use the Kotlin API.** The scaffolder
  moved to 0.100.0, and its Kotlin templates depend on
  `dev.ancaria.coderpack:api-kotlin`. A generated Kotlin mod extends the
  `SacredMod` class from that module, gets its context as the receiver of
  `Context.load()`, and registers listeners with `on<Hero> { }` instead of an
  annotated method. Java and Groovy projects are unchanged, and `@Subscribe`
  still works in Kotlin for anybody who prefers it. Nothing in the wizard
  itself changed: the dialog reads its templates, languages and build DSLs
  from the scaffolder, so it offers the new ones without knowing their names.

## 0.99.0

The first release.

- **New Project | Sacred Mod.** Group, mod name, mod id derived from it,
  description, template, the language the mod is written in (Java, Kotlin or
  Groovy) and the syntax its build is typed in (Kotlin DSL or Groovy DSL).
  Checkboxes for a git repository and for the SRML layout that lets a launcher
  be pointed straight at the project. Author, package and mod version under
  Advanced. What it writes is what `coderpack new` writes, from the same
  templates.
- **Run Sacred.** Builds the mod, installs it into the game folder, makes sure
  the chosen Sacred Mod Loader release is there, and starts it. A project that
  is a Sacred mod gets the configuration when it opens.
- **Gutter icons** beside the class the mod descriptor names as its entrypoint,
  and beside every `@Subscribe` method that listens to a game event. Java,
  Kotlin and Groovy.
- **Settings | Tools | Sacred Mod Development.** The Sacred Gold folder, and
  which loader release to run with. Releases are listed from GitHub and cached
  under the Gradle cache directory.
