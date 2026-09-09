package dev.ancaria.idea.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import dev.ancaria.idea.Sacred
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.settings.SacredConfigurable
import javax.swing.JComponent

/**
 * Two checkboxes and a way to the settings.
 *
 * Everything else this run needs (the folder, the release) lives in
 * Settings and is shown here as a link rather than copied into a field, so
 * there is one place to change it and no chance of the two disagreeing.
 */
class SacredRunEditor : SettingsEditor<SacredRunConfiguration>() {

    private var installMod = true
    private var showConsole = false

    private val form: DialogPanel = panel {
        row {
            checkBox(SacredBundle.message("run.editor.install"))
                .bindSelected({ installMod }, { installMod = it })
        }.rowComment(SacredBundle.message("run.editor.install.comment", Sacred.INSTALL_TASK))

        row {
            checkBox(SacredBundle.message("run.editor.console"))
                .bindSelected({ showConsole }, { showConsole = it })
        }.rowComment(SacredBundle.message("run.editor.console.comment"))

        row {
            link(SacredBundle.message("run.editor.settings")) {
                ShowSettingsUtil.getInstance().showSettingsDialog(null, SacredConfigurable::class.java)
            }
        }
    }

    override fun resetEditorFrom(configuration: SacredRunConfiguration) {
        installMod = configuration.installMod
        showConsole = configuration.showConsole
        form.reset()
    }

    override fun applyEditorTo(configuration: SacredRunConfiguration) {
        form.apply()
        configuration.installMod = installMod
        configuration.showConsole = showConsole
    }

    override fun createEditor(): JComponent = form
}
