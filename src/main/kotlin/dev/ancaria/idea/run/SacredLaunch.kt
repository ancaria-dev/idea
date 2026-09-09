package dev.ancaria.idea.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import dev.ancaria.idea.Notices
import dev.ancaria.idea.Sacred
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.launcher.LauncherInstall
import dev.ancaria.idea.settings.GameFolder
import dev.ancaria.idea.settings.SacredSettings
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.isRegularFile

/**
 * The two pieces of work that have to happen before anything starts, and the
 * command lines that then do the starting.
 *
 * Provisioning is here rather than in the run state because of when it can
 * happen: downloading a release and copying five megabytes wants a progress
 * dialog, and a progress dialog wants the event thread, which is exactly what
 * `getState` has and `startProcess` may not.
 */
object SacredLaunch {

    /**
     * The loader the settings ask for, in the game folder, ready to start.
     *
     * Runs under a modal progress because the first call on a new machine
     * downloads a release. `invokeAndWait` runs its body directly when it is
     * already on the event thread, so this is correct from either.
     */
    fun provision(project: Project, game: Path): Path {
        val settings = SacredSettings.getInstance()
        var failure: Exception? = null
        var copied = false

        ApplicationManager.getApplication().invokeAndWait {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    try {
                        val cached = LauncherInstall.provide(
                            settings,
                            ProgressManager.getInstance().progressIndicator,
                        )
                        copied = LauncherInstall.sync(cached, game)
                    } catch (problem: Exception) {
                        failure = problem
                    }
                },
                SacredBundle.message("launcher.progress"),
                true,
                project,
            )
        }

        failure?.let { throw ExecutionException(it.message, it) }
        if (copied) {
            Notices.info(
                project,
                SacredBundle.message("run.type.name"),
                SacredBundle.message("launcher.copied", game),
            )
        }
        return GameFolder.loader(game)
    }

    /**
     * `gradlew installSacredMod -PsacredDir=<game>`, or nothing.
     *
     * Nothing when the project has no wrapper, which is a checkout somebody
     * imported rather than one this plugin wrote. Starting the game is still
     * worth doing there. Failing the run over a missing `gradlew` is not.
     *
     * The task is named bare rather than qualified, so a repository with one
     * mod at the root and one with a directory per mod both build everything
     * they have, which is the same thing SRML's own workflow does.
     */
    fun build(project: Project, game: Path): GeneralCommandLine? {
        val base = project.basePath?.let(Path::of) ?: return null
        val wrapper = base.resolve(if (SystemInfo.isWindows) "gradlew.bat" else "gradlew")
        if (!wrapper.isRegularFile()) return null
        return GeneralCommandLine(wrapper.toString())
            .withWorkingDirectory(base)
            .withParameters(
                Sacred.INSTALL_TASK,
                "-P${Sacred.SACRED_DIR_PROPERTY}=$game",
                // Gradle's rich output is a stream of terminal escapes in a
                // console that does not read them.
                "--console=plain",
            )
            .withCharset(StandardCharsets.UTF_8)
    }

    fun start(loader: Path, game: Path, console: Boolean): GeneralCommandLine =
        GeneralCommandLine(loader.toString())
            .withWorkingDirectory(game)
            .withCharset(StandardCharsets.UTF_8)
            .apply { if (console) addParameter("--debug") }
}
