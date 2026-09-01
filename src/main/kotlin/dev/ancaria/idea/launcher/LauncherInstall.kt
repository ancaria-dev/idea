package dev.ancaria.idea.launcher

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.settings.GameFolder
import dev.ancaria.idea.settings.SacredSettings
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.isRegularFile

/** Something a person can fix, said in one sentence. Never a stack trace. */
class LauncherProblem(message: String) : RuntimeException(message)

/**
 * Getting the right Sacred Mod Loader into the game folder.
 *
 * Two steps that look like one. The release the settings name has to be in the
 * cache, which means downloading it the first time; and the executable in the
 * game folder has to be that same file, which means comparing it and copying
 * when it is not. The second step is why the version in Settings does anything
 * at all: without it, changing the number would change a cache directory
 * nobody reads.
 *
 * The comparison is over the bytes rather than over a timestamp or a version
 * string. There is nothing in the game folder that says which release the exe
 * there is, a copy that stopped halfway leaves a file with a perfectly good
 * date on it, and hashing five megabytes costs a few milliseconds against
 * starting a game.
 */
object LauncherInstall {

    private val LOG = logger<LauncherInstall>()

    /**
     * The cached executable for the release the settings ask for.
     *
     * Blank in Settings means the newest release there is. With no network that
     * cannot be looked up, so the newest one already in the cache is used
     * instead and said out loud in the log: a launcher somebody has is better
     * than an error about one they might not need.
     */
    fun provide(settings: SacredSettings, indicator: ProgressIndicator?): Path {
        val wanted = settings.launcherVersion
        if (wanted.isNotEmpty()) {
            if (LauncherCache.has(wanted)) return LauncherCache.file(wanted)
            val release = LauncherReleases.find(wanted)
                ?: throw LauncherProblem(SacredBundle.message("launcher.error.missing", wanted))
            indicator?.text = SacredBundle.message("launcher.downloading", release.version)
            return LauncherCache.download(release, indicator)
        }

        val latest = LauncherReleases.latest()
        if (latest == null) {
            val fallback = LauncherCache.versions().firstOrNull()
                ?: throw LauncherProblem(SacredBundle.message("launcher.error.offline"))
            LOG.info("no release list; falling back to the cached launcher $fallback")
            return LauncherCache.file(fallback)
        }
        if (LauncherCache.has(latest.version)) return LauncherCache.file(latest.version)
        indicator?.text = SacredBundle.message("launcher.downloading", latest.version)
        return LauncherCache.download(latest, indicator)
    }

    /**
     * Puts that executable in the game folder when what is there is not it.
     *
     * Returns true when it copied, which is worth one line in the run console:
     * a player who did not expect the loader to change should be able to see
     * that it did.
     */
    fun sync(cached: Path, game: Path): Boolean {
        val installed = GameFolder.loader(game)
        if (installed.isRegularFile() && sha256(installed) == sha256(cached)) return false
        Files.copy(cached, installed, StandardCopyOption.REPLACE_EXISTING)
        return true
    }

    private fun sha256(file: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(file).use { stream ->
            val buffer = ByteArray(1 shl 16)
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
