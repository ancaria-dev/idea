package dev.ancaria.idea.settings

import dev.ancaria.idea.Sacred
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/**
 * Whether a directory is a copy of Sacred Gold, asked the way everything else
 * in this project asks it.
 *
 * The launcher, the host and the agent all try `pureHD.exe`, then `Sacred.exe`,
 * then `Game.exe`, ignoring case, and the addresses belong to the first of
 * those. This does the same, so a folder the launcher would start is a folder
 * this plugin accepts, and one it would not is refused in a dialog rather than
 * at the moment somebody presses Run.
 *
 * Case-insensitively and by listing rather than by [Path.resolve]: the checkout
 * this is developed in is on Windows, where it makes no difference, and a
 * player running the game through Wine on a case-sensitive filesystem is a
 * perfectly ordinary way to play a game from 2004.
 */
object GameFolder {

    /** The path somebody typed, or nothing when it is blank or not a path at all. */
    fun of(text: String?): Path? {
        val trimmed = text?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        return try {
            Path.of(trimmed)
        } catch (notAPath: InvalidPathException) {
            null
        }
    }

    /** The game's executable in this folder, in the order the launcher tries them. */
    fun executable(folder: Path?): Path? {
        if (folder == null || !folder.isDirectory()) return null
        val here = try {
            folder.listDirectoryEntries().filter { it.isRegularFile() }
        } catch (unreadable: java.io.IOException) {
            return null
        }
        for (wanted in Sacred.EXECUTABLES) {
            here.firstOrNull { it.name.equals(wanted, ignoreCase = true) }?.let { return it }
        }
        return null
    }

    fun holdsGame(folder: Path?): Boolean = executable(folder) != null

    /** Where a launcher looks for installed mods. */
    fun mods(folder: Path): Path = folder.resolve(Sacred.MODS_DIR)

    /** Where the loader executable has to be for anything to start. */
    fun loader(folder: Path): Path = folder.resolve(Sacred.LAUNCHER_FILE)
}
