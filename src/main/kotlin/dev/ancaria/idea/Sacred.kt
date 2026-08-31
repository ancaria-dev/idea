package dev.ancaria.idea

/**
 * The names this plugin shares with the rest of the toolchain.
 *
 * Every one of them is a decision made in another repository -- the API a mod
 * implements, the file the loader is released as, the task the Gradle plugin
 * registers -- and the only thing that keeps the two ends together is that
 * neither invents a second spelling. So they are here, once, rather than inline
 * at the four call sites that would each have to be found again when one of
 * them changes.
 */
object Sacred {

    const val SITE = "https://ancaria.dev"

    /** Public, so the release list is one unauthenticated GET away. */
    const val LAUNCHER_REPO = "ancaria-dev/launcher"

    /** What `launcher`'s release workflow attaches to every tag, verbatim. */
    const val LAUNCHER_FILE = "Sacred Mod Loader.exe"

    /**
     * The three names the launcher, the host and the agent all try, in order.
     * The addresses belong to the first; the other two start and are told so.
     */
    val EXECUTABLES = listOf("pureHD.exe", "Sacred.exe", "Game.exe")

    /** Where a launcher looks for installed mods, under the game folder. */
    const val MODS_DIR = "mods"

    /** What the Gradle plugin calls the task that copies a jar into [MODS_DIR]. */
    const val INSTALL_TASK = "installSacredMod"

    /** The property that task reads for the game folder. */
    const val SACRED_DIR_PROPERTY = "sacredDir"

    const val MOD_INTERFACE = "dev.ancaria.coderpack.api.SacredMod"

    const val SUBSCRIBE = "dev.ancaria.coderpack.api.Subscribe"

    /**
     * Everything in here is an event except `Guard`, which is the one class in
     * the package that does not extend `Event`. The linter draws the same line.
     */
    const val EVENT_PACKAGE = "dev.ancaria.coderpack.api.event"

    const val EVENT_BASE = "$EVENT_PACKAGE.Event"

    /** The balloon group declared in plugin.xml. */
    const val NOTIFICATIONS = "Sacred Mod Development"
}
