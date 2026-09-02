package dev.ancaria.idea

import dev.ancaria.idea.settings.GameFolder
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Whether a folder is the game, asked the way the launcher asks it. */
class GameFolderTest {

    private val directory = createTempDirectory("sacred-folder")

    @AfterTest
    fun cleanUp() {
        directory.toFile().deleteRecursively()
    }

    @Test
    fun `finds the executable whatever case it is spelled in`() {
        val game = directory.resolve("game").createDirectory()
        game.resolve("PUREHD.EXE").writeText("")
        assertTrue(GameFolder.holdsGame(game))
        assertEquals("PUREHD.EXE", GameFolder.executable(game)?.name)
    }

    @Test
    fun `prefers the build the addresses belong to`() {
        // All three start, and the address table is the wrapper's. When a folder
        // has more than one, the one this project was built against wins, which
        // is the order the launcher, the host and the agent all use.
        val game = directory.resolve("both").createDirectory()
        game.resolve("Sacred.exe").writeText("")
        game.resolve("pureHD.exe").writeText("")
        assertEquals("pureHD.exe", GameFolder.executable(game)?.name)
    }

    @Test
    fun `refuses a folder with no game in it`() {
        val empty = directory.resolve("empty").createDirectory()
        assertFalse(GameFolder.holdsGame(empty))
        assertNull(GameFolder.executable(empty))
    }

    @Test
    fun `refuses a blank path and one that is not a path at all`() {
        assertNull(GameFolder.of(null))
        assertNull(GameFolder.of("   "))
        assertFalse(GameFolder.holdsGame(null))
        // Never a crash: an unconfigured setting and a typo have to end in the
        // same place, which is the dialog saying so.
        assertFalse(GameFolder.holdsGame(GameFolder.of("C:\u0000nope")))
    }

    @Test
    fun `says where mods and the loader go`() {
        val game = directory.resolve("paths").createDirectory()
        assertEquals("mods", GameFolder.mods(game).name)
        assertEquals("Sacred Mod Loader.exe", GameFolder.loader(game).name)
    }
}
