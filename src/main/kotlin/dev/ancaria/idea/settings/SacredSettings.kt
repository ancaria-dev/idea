package dev.ancaria.idea.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import java.nio.file.Path

class SacredState : BaseState() {

    /** The Sacred Gold folder. Empty until somebody says, and nothing runs until then. */
    var gamePath: String? by string()

    /**
     * The Sacred Mod Loader release to run with.
     *
     * Empty means whatever the latest release is, which is what most people
     * want and what the dialog shows as `Latest (0.1.20)`. A number means that
     * number, forever, which is what somebody debugging against one release
     * wants. There is no third state: an unset field that quietly followed the
     * latest release and then stopped would be worse than either.
     */
    var launcherVersion: String? by string()
}

/**
 * Where the game is, and which loader to start it with.
 *
 * Application level rather than project level, because both answers are facts
 * about this machine rather than about a mod: somebody with four mod projects
 * has one copy of Sacred Gold, and typing the path into each of them is four
 * chances to type it differently.
 */
@Service(Service.Level.APP)
@State(name = "Sacred", storages = [Storage("sacred.xml")])
class SacredSettings : SimplePersistentStateComponent<SacredState>(SacredState()) {

    /** The configured folder, or nothing. Not checked here: see [GameFolder]. */
    var gameFolder: Path?
        get() = GameFolder.of(state.gamePath)
        set(value) {
            state.gamePath = value?.toString()
        }

    var gamePath: String
        get() = state.gamePath.orEmpty()
        set(value) {
            state.gamePath = value.trim().ifEmpty { null }
        }

    /** Blank for "the latest release", which is what the dialog offers first. */
    var launcherVersion: String
        get() = state.launcherVersion.orEmpty()
        set(value) {
            state.launcherVersion = value.trim().ifEmpty { null }
        }

    val ready: Boolean
        get() = GameFolder.holdsGame(gameFolder)

    companion object {
        fun getInstance(): SacredSettings = service()
    }
}
