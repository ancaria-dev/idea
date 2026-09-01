package dev.ancaria.idea.launcher

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.io.HttpRequests
import dev.ancaria.idea.Sacred
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * Every launcher release this machine has downloaded, one directory each.
 *
 *     ~/.gradle/caches/ancaria/launcher/0.1.20/Sacred Mod Loader.exe
 *
 * Under the Gradle cache because that is the directory a JVM developer already
 * expects to be large, already excludes from backups, and already clears when
 * they want the disk back. Nothing is ever deleted from here: an old release is
 * a few megabytes and the reason somebody pinned one is usually that they are
 * comparing it against another.
 */
object LauncherCache {

    fun root(): Path {
        val gradle = System.getenv("GRADLE_USER_HOME")
            ?.takeIf { it.isNotBlank() }
            ?.let(Path::of)
            ?: Path.of(System.getProperty("user.home"), ".gradle")
        return gradle.resolve("caches").resolve("ancaria").resolve("launcher")
    }

    fun file(version: String): Path = root().resolve(version).resolve(Sacred.LAUNCHER_FILE)

    fun has(version: String): Boolean = file(version).isRegularFile()

    /** What is already here, newest-looking first, for a dialog with no network. */
    fun versions(): List<String> {
        val root = root()
        if (!root.isDirectory()) return emptyList()
        return root.listDirectoryEntries()
            .filter { it.isDirectory() && it.resolve(Sacred.LAUNCHER_FILE).isRegularFile() }
            .map { it.name }
            .sortedWith(compareByDescending(VersionOrder) { it })
    }

    /**
     * Downloads one release, unless it is already here.
     *
     * Into a `.part` beside the target and then moved, so an interrupted
     * download is never mistaken for a launcher: the alternative is a
     * half-written executable that exists, passes [has], and fails to start.
     */
    fun download(release: LauncherRelease, indicator: ProgressIndicator?): Path {
        val target = file(release.version)
        if (target.isRegularFile()) return target
        target.parent.createDirectories()
        val partial = target.resolveSibling(target.name + ".part")
        try {
            HttpRequests.request(release.url)
                .productNameAsUserAgent()
                .saveToFile(partial.toFile(), indicator)
            Files.move(
                partial,
                target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } finally {
            partial.deleteIfExists()
        }
        return target
    }

    /** `0.1.9` is older than `0.1.20`, which string order gets backwards. */
    private object VersionOrder : Comparator<String> {
        override fun compare(left: String, right: String): Int {
            val a = left.split('.').map { it.toIntOrNull() ?: 0 }
            val b = right.split('.').map { it.toIntOrNull() ?: 0 }
            for (at in 0 until maxOf(a.size, b.size)) {
                val difference = (a.getOrElse(at) { 0 }).compareTo(b.getOrElse(at) { 0 })
                if (difference != 0) return difference
            }
            return left.compareTo(right)
        }
    }
}
