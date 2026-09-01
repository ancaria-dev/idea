package dev.ancaria.idea.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import dev.ancaria.idea.Sacred
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.launcher.LauncherCache
import dev.ancaria.idea.launcher.LauncherInstall
import dev.ancaria.idea.launcher.LauncherProblem
import dev.ancaria.idea.launcher.LauncherReleases

/**
 * One release to choose from. The empty [version] is "whatever the latest is".
 *
 * Two of these are the same when they name the same release, label or no label.
 * That matters because the label of the first row changes from `Latest` to
 * `Latest (0.1.20)` the moment GitHub answers, and a settings page that decided
 * it was modified because a label grew would offer Apply for a change nobody
 * made.
 */
private class VersionChoice(val version: String, private val label: String) {

    override fun toString(): String = label

    override fun equals(other: Any?): Boolean =
        other is VersionChoice && other.version == version

    override fun hashCode(): Int = version.hashCode()
}

/**
 * Settings | Tools | Sacred Mod Development.
 *
 * Two answers, and the second one only matters because of the first: which
 * folder the game is in, and which release of the loader to start it with.
 *
 * The release list comes off GitHub, which means it is not there when the
 * dialog opens. Rather than blocking the settings page on a network call, the
 * combo is filled from what is already downloaded, the request runs on a pooled
 * thread, and the list grows a moment later. A dialog that is usable
 * immediately and complete shortly afterwards beats one that is neither for two
 * seconds.
 */
class SacredConfigurable : BoundConfigurable(SacredBundle.message("settings.title")) {

    private val settings = SacredSettings.getInstance()

    private val versions = MutableCollectionComboBoxModel(offline())

    override fun createPanel(): DialogPanel {
        loadReleases()
        return panel {
            group(SacredBundle.message("settings.game.group")) {
                row(SacredBundle.message("settings.game.path")) {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle(SacredBundle.message("settings.game.choose"))
                    )
                        .align(AlignX.FILL)
                        .bindText({ settings.gamePath }, { settings.gamePath = it })
                        .validationOnApply { field ->
                            val folder = GameFolder.of(field.text)
                            when {
                                folder == null -> null
                                GameFolder.holdsGame(folder) -> null
                                else -> error(
                                    SacredBundle.message(
                                        "settings.game.error",
                                        Sacred.EXECUTABLES.joinToString(", "),
                                    )
                                )
                            }
                        }
                }.rowComment(SacredBundle.message("settings.game.comment"))
            }

            group(SacredBundle.message("settings.launcher.group")) {
                row(SacredBundle.message("settings.launcher.version")) {
                    comboBox(versions).bindItem(
                        { chosen() },
                        { choice -> settings.launcherVersion = choice?.version.orEmpty() },
                    )
                    link(SacredBundle.message("settings.launcher.refresh")) {
                        loadReleases(refresh = true)
                    }
                }.rowComment(
                    SacredBundle.message("settings.launcher.comment", LauncherCache.root())
                )
            }

            row {
                browserLink(SacredBundle.message("wizard.site"), Sacred.SITE)
            }
        }
    }

    /**
     * Applying a version is what downloads it.
     *
     * Not at Run time and not in the background: somebody who just picked
     * 0.1.14 out of a list should find out here whether it can be fetched, and
     * a modal progress over one file is the honest way to say "this is going to
     * take a moment".
     */
    override fun apply() {
        super.apply()
        try {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(
                {
                    LauncherInstall.provide(
                        settings,
                        ProgressManager.getInstance().progressIndicator,
                    )
                },
                SacredBundle.message("launcher.progress"),
                true,
                null,
            )
        } catch (problem: LauncherProblem) {
            throw ConfigurationException(problem.message)
        } catch (failed: java.io.IOException) {
            throw ConfigurationException(
                SacredBundle.message("launcher.error.download", failed.message.orEmpty())
            )
        }
    }

    /** What the combo shows before, and without, an answer from GitHub. */
    private fun offline(): MutableList<VersionChoice> {
        val choices = mutableListOf(VersionChoice("", SacredBundle.message("settings.launcher.latest")))
        LauncherCache.versions().forEach { choices += VersionChoice(it, it) }
        val pinned = settings.launcherVersion
        if (pinned.isNotEmpty() && choices.none { it.version == pinned }) {
            choices += VersionChoice(pinned, pinned)
        }
        return choices
    }

    private fun chosen(): VersionChoice {
        val pinned = settings.launcherVersion
        return versions.items.firstOrNull { it.version == pinned } ?: versions.items.first()
    }

    private fun loadReleases(refresh: Boolean = false) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val released = LauncherReleases.list(refresh)
            ApplicationManager.getApplication().invokeLater {
                val latest = released.firstOrNull()
                val head = VersionChoice(
                    "",
                    if (latest == null) {
                        SacredBundle.message("settings.launcher.latest")
                    } else {
                        SacredBundle.message("settings.launcher.latest.known", latest.version)
                    },
                )
                val rest = (released.map { it.version } + LauncherCache.versions())
                    .distinct()
                    .map { VersionChoice(it, it) }
                val selected = versions.selected?.version.orEmpty()
                versions.update(listOf(head) + rest)
                versions.selectedItem =
                    (listOf(head) + rest).firstOrNull { it.version == selected } ?: head
            }
        }
    }
}
