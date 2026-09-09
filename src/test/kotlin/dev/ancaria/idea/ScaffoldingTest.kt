package dev.ancaria.idea

import dev.ancaria.idea.wizard.Scaffolding
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * What the dialog writes, without the dialog.
 *
 * This is the seam that matters: everything above it is Swing and everything
 * below it is the scaffolder that `coderpack new` uses, so the question here is
 * whether the answers on the form arrive as the right eleven placeholders. The
 * project it produces is already known to build: that is the end-to-end test
 * in ancaria-dev/build, run against these same templates.
 */
class ScaffoldingTest {

    private val directory = createTempDirectory("sacred-scaffold")

    @AfterTest
    fun cleanUp() {
        directory.toFile().deleteRecursively()
    }

    @Test
    fun `writes a Kotlin mod with a Kotlin DSL build and an SRML registry`() {
        val root = write(language = "kotlin", dsl = "kotlin", srml = true)

        val build = root.resolve("build.gradle.kts").readText()
        assertTrue("""id = "demo-mod"""" in build, build)
        assertTrue("""displayName = "Demo Mod"""" in build, build)
        assertTrue("""entrypoint = "dev.example.demomod.DemoMod"""" in build, build)
        assertTrue("""kotlin("jvm") version""" in build, build)

        assertTrue(root.resolve("src/main/kotlin/dev/example/demomod/DemoMod.kt").exists())
        assertTrue(root.resolve("settings.gradle.kts").exists())
        assertTrue(root.resolve("registry.toml").exists())
        assertTrue(root.resolve(".github/workflows/build.yml").exists())
        // One command has to be enough, which means the project brings a Gradle.
        assertTrue(root.resolve("gradlew").exists())
        assertTrue(root.resolve("gradle/wrapper/gradle-wrapper.jar").exists())

        val registry = root.resolve("registry.toml").readText()
        assertTrue("""url = "https://github.com/someone/demo-mod"""" in registry, registry)
    }

    @Test
    fun `writes a Java mod with a Groovy DSL build and no registry`() {
        val root = write(language = "java", dsl = "groovy", srml = false)

        assertTrue(root.resolve("build.gradle").exists())
        assertTrue(root.resolve("settings.gradle").exists())
        assertFalse(root.resolve("build.gradle.kts").exists())
        assertFalse(root.resolve("settings.gradle.kts").exists())

        // The two files SRML is, and nothing else, is what the checkbox drops.
        assertFalse(root.resolve("registry.toml").exists())
        assertFalse(root.resolve(".github/workflows/build.yml").exists())
        assertTrue(root.resolve(".gitignore").exists())
        assertTrue(root.resolve("src/main/java/dev/example/demomod/DemoMod.java").exists())

        val build = root.resolve("build.gradle").readText()
        assertTrue("""id 'dev.ancaria.coderpack' version""" in build, build)
        // The mod is Java, whatever the build script is typed in.
        assertFalse("org.jetbrains.kotlin" in build, build)
    }

    @Test
    fun `writes into a directory the IDE has already made`() {
        // Unlike the command line, this always writes into an existing
        // directory: the project, and its .idea, are there before the wizard's
        // last step runs.
        val root = directory.resolve("taken")
        root.toFile().mkdirs()
        root.resolve("notes.txt").toFile().writeText("mine")
        write(language = "java", dsl = "kotlin", srml = true, at = root)
        assertTrue(root.resolve("build.gradle.kts").exists())
        assertEquals("mine", root.resolve("notes.txt").readText())
    }

    private fun write(
        language: String,
        dsl: String,
        srml: Boolean,
        at: Path = directory.resolve("$language-$dsl-$srml"),
    ): Path {
        Scaffolding.write(
            Scaffolding.Request(
                root = at,
                id = "demo-mod",
                displayName = "Demo Mod",
                description = "Proves the dialog reaches the scaffolder",
                version = "1.0.0",
                pkg = "dev.example.demomod",
                author = "Somebody",
                repository = "https://github.com/someone/demo-mod",
                template = "mod",
                language = language,
                dsl = dsl,
                srml = srml,
                // Left off everywhere: a test that ran `git init` would leave a
                // repository in a temp directory and need git on the machine.
                git = false,
            )
        )
        return at
    }
}
