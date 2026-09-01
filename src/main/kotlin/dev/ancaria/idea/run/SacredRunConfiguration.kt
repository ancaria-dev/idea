package dev.ancaria.idea.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.ConfigurationQuickFix
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.settings.GameFolder
import dev.ancaria.idea.settings.SacredConfigurable
import dev.ancaria.idea.settings.SacredSettings
import java.nio.file.Path

/**
 * One configuration, and it knows almost nothing.
 *
 * Where the game is and which loader to use are settings rather than fields
 * here, so this holds two checkboxes and the rest is worked out at the moment
 * Run is pressed. That is deliberate: a run configuration is a file people
 * commit, and a committed absolute path to somebody else's Steam library is a
 * bug report.
 */
class SacredRunConfiguration(
    project: Project,
    factory: ConfigurationFactory,
    name: String,
) : RunConfigurationBase<SacredRunOptions>(project, factory, name) {

    private val settings: SacredRunOptions
        get() = options as SacredRunOptions

    var installMod: Boolean
        get() = settings.installMod
        set(value) {
            settings.installMod = value
        }

    var showConsole: Boolean
        get() = settings.showConsole
        set(value) {
            settings.showConsole = value
        }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = SacredRunEditor()

    /**
     * The one thing that can be wrong before anything is started, with the
     * button that fixes it attached: an error somebody cannot act on from where
     * they are standing is an error they read twice.
     */
    override fun checkConfiguration() {
        if (SacredSettings.getInstance().ready) return
        // Spelled as a ConfigurationQuickFix rather than a Runnable: both
        // overloads exist and a lambda picks neither.
        throw RuntimeConfigurationError(
            SacredBundle.message("run.error.game"),
            ConfigurationQuickFix {
                ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, SacredConfigurable::class.java)
            },
        )
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        val game = gameFolder()
        // Downloads the release if this machine has not got it, and copies it
        // into the game folder when what is there is a different build.
        val loader = SacredLaunch.provision(project, game)
        return SacredRunState(
            environment,
            build = if (installMod) SacredLaunch.build(project, game) else null,
            launch = SacredLaunch.start(loader, game, showConsole),
        )
    }

    /**
     * Pressing Run with nothing configured opens the page that configures it.
     *
     * The alternative is an error dialog with a settings path in it, which is
     * the same click with a step in front of it. `invokeAndWait` runs inline
     * when this is already the event thread, so it is right from either.
     */
    private fun gameFolder(): Path {
        val settings = SacredSettings.getInstance()
        if (!settings.ready) {
            ApplicationManager.getApplication().invokeAndWait {
                ShowSettingsUtil.getInstance()
                    .showSettingsDialog(project, SacredConfigurable::class.java)
            }
        }
        val folder = settings.gameFolder
        if (!GameFolder.holdsGame(folder)) {
            throw ExecutionException(SacredBundle.message("run.error.game"))
        }
        return folder!!
    }
}
