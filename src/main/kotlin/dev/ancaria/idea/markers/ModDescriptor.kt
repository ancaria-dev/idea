package dev.ancaria.idea.markers

import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

/**
 * What the `sacred { }` block says, read out of the build script.
 *
 * This is the metadata half of the entrypoint marker. The PSI half can only see
 * that a class implements `SacredMod`, which any number of classes in a project
 * may do. The descriptor is the one place that says which of them the loader
 * will actually instantiate. Neither answer alone is the question a reader has.
 *
 * Read with a regular expression rather than with the Kotlin or Groovy PSI,
 * deliberately. The line is `entrypoint = "..."` in both syntaxes, the file is
 * a build script that may not be resolvable at all until Gradle has synced, and
 * a marker that disappears during a sync is worse than one that reads a string
 * literal out of a file. The result is cached against the build script, so it
 * is re-read when that file changes and never on a keystroke elsewhere.
 */
object ModDescriptor {

    /** `entrypoint = "a.b.C"` in either DSL, and `entrypoint.set("a.b.C")` in older Groovy. */
    private val ENTRYPOINT = Regex("""entrypoint\s*(?:=|\.set\s*\()\s*["']([^"'\s]+)["']""")

    /** The plugin id, which is what makes a Gradle project a Sacred mod project. */
    private const val PLUGIN = "dev.ancaria.coderpack"

    private val BUILD_FILES = listOf("build.gradle.kts", "build.gradle")

    /** The class the descriptor names, and where it says so. */
    data class Declaration(val file: PsiFile, val entrypoint: String, val offset: Int)

    fun declaration(element: PsiElement): Declaration? {
        val build = buildFile(element) ?: return null
        return CachedValuesManager.getCachedValue(build) {
            CachedValueProvider.Result.create(read(build), build)
        }
    }

    /**
     * Whether this project is a Sacred mod at all.
     *
     * Asked once, when a project opens, to decide whether to offer a Run Sacred
     * configuration. The root and one level below it, because SRML allows both
     * one mod at the root and a directory per mod.
     */
    fun looksSacred(project: Project): Boolean {
        val root = project.guessProjectDir() ?: return false
        if (applies(root)) return true
        return (root.children ?: emptyArray()).any { it.isDirectory && applies(it) }
    }

    private fun applies(directory: VirtualFile): Boolean =
        BUILD_FILES.asSequence()
            .mapNotNull { directory.findChild(it) }
            .any { file ->
                runCatching { PLUGIN in String(file.contentsToByteArray(), Charsets.UTF_8) }
                    .getOrDefault(false)
            }

    private fun read(build: PsiFile): Declaration? {
        val found = ENTRYPOINT.find(build.text) ?: return null
        return Declaration(build, found.groupValues[1], found.range.first)
    }

    /** The build script of the module this element is in, whichever DSL it is in. */
    private fun buildFile(element: PsiElement): PsiFile? {
        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return null
        val manager = PsiManager.getInstance(element.project)
        for (root in ModuleRootManager.getInstance(module).contentRoots) {
            for (name in BUILD_FILES) {
                val file = root.findChild(name) ?: continue
                return manager.findFile(file)
            }
        }
        return null
    }
}
