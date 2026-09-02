package dev.ancaria.idea.run

import com.intellij.execution.RunManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.markers.ModDescriptor
import org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject
import org.jetbrains.plugins.gradle.settings.GradleSettings

/**
 * What a Sacred mod project gets when it opens: a linked Gradle build, and a
 * Run Sacred configuration.
 *
 * Both are here rather than in the wizard because most projects are opened
 * rather than created -- a clone, a checkout on a second machine, a project
 * somebody started with `coderpack new` in a terminal -- and each of those
 * should have the green arrow the moment it opens. A project the wizard just
 * wrote comes through here too, on the same path, which is one path to keep
 * working instead of two.
 *
 * Neither is done twice. The build is linked only when Gradle has nothing
 * linked at all, which is true of a project the wizard has just written and
 * false of one the IDE imported on open; the configuration is created only when
 * there is not one.
 */
class SacredRunSetup : ProjectActivity {

    override suspend fun execute(project: Project) {
        if (!ModDescriptor.looksSacred(project)) return

        val base = project.basePath
        if (base != null && GradleSettings.getInstance(project).linkedProjectsSettings.isEmpty()) {
            LOG.info("linking the Gradle build at $base")
            // Deprecated in favour of a suspend function that does not exist in
            // the oldest IDE this plugin supports. The deprecation is a
            // warning; the missing function would be a crash.
            @Suppress("DEPRECATION")
            linkAndRefreshGradleProject(base, project)
        }

        ApplicationManager.getApplication().invokeLater(
            { ensure(project) },
            ModalityState.nonModal(),
            project.disposed,
        )
    }

    companion object {

        private val LOG = logger<SacredRunSetup>()

        fun ensure(project: Project) {
            val manager = RunManager.getInstance(project)
            val type = SacredRunConfigurationType.getInstance()
            if (manager.getConfigurationSettingsList(type).isNotEmpty()) return

            val settings = manager.createConfiguration(
                SacredBundle.message("run.name"),
                type.configurationFactories.first(),
            )
            manager.addConfiguration(settings)
            // Selected only when nothing else is, so opening a mod project does
            // not quietly take over from whatever somebody was last running.
            if (manager.selectedConfiguration == null) {
                manager.selectedConfiguration = settings
            }
        }
    }
}
