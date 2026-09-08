# Changelog

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
