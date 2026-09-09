package dev.ancaria.idea.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import dev.ancaria.idea.Notices
import dev.ancaria.idea.SacredBundle

/**
 * Build the mod, then start the game.
 *
 * The console follows Gradle rather than the loader, which is the decision
 * worth explaining. A build that fails is the thing a mod author needs to read;
 * a launcher that started is a window on their screen. So the run window shows
 * the build, and the game is started from the listener when the build came back
 * zero, detached, because somebody alt-tabbing out of Sacred Gold should not
 * be looking at a Stop button that kills it.
 *
 * With nothing to build (no wrapper in the project) the console follows the
 * loader instead, and Stop does what it says.
 */
class SacredRunState(
    environment: ExecutionEnvironment,
    private val build: GeneralCommandLine?,
    private val launch: GeneralCommandLine,
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        if (build == null) {
            return handler(launch)
        }
        val gradle = handler(build)
        gradle.addProcessListener(object : ProcessListener {
            override fun processTerminated(event: ProcessEvent) {
                if (event.exitCode != 0) return
                try {
                    launch.createProcess()
                } catch (failed: ExecutionException) {
                    // The console is closed by the time this runs, so the only
                    // place left to say it is a balloon.
                    Notices.error(
                        environment.project,
                        SacredBundle.message("run.type.name"),
                        SacredBundle.message("run.error.start", failed.message.orEmpty()),
                    )
                }
            }
        })
        return gradle
    }

    private fun handler(command: GeneralCommandLine): OSProcessHandler {
        val handler = OSProcessHandler(command)
        ProcessTerminatedListener.attach(handler)
        return handler
    }
}
