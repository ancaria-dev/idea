package dev.ancaria.idea.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.SacredIcons

/**
 * `Run Sacred`: the one green arrow a mod author presses.
 *
 * There is exactly one factory and it has no options worth choosing between,
 * which is why this is a [ConfigurationTypeBase] and not a type with a family
 * of them. What varies between two mod projects is the mod, not the way it is
 * started.
 */
class SacredRunConfigurationType : ConfigurationTypeBase(
    ID,
    SacredBundle.message("run.type.name"),
    SacredBundle.message("run.type.description"),
    NotNullLazyValue.createValue { SacredIcons.Sacred },
) {

    init {
        addFactory(SacredConfigurationFactory(this))
    }

    companion object {

        const val ID: String = "SacredModLoader"

        fun getInstance(): SacredRunConfigurationType =
            ConfigurationTypeUtil.findConfigurationType(SacredRunConfigurationType::class.java)
    }
}

class SacredConfigurationFactory(type: SacredRunConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String = SacredRunConfigurationType.ID

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        SacredRunConfiguration(project, this, SacredBundle.message("run.name"))

    override fun getOptionsClass(): Class<out RunConfigurationOptions> =
        SacredRunOptions::class.java
}

/**
 * What is remembered per configuration.
 *
 * Not the game folder and not the launcher version: those are settings, they
 * are the same for every project on the machine, and a copy of them in a run
 * configuration is a copy somebody commits and everybody else has to fix.
 */
class SacredRunOptions : RunConfigurationOptions() {

    /** Build the mod and copy it into the game folder before starting. */
    var installMod: Boolean by property(true)

    /** `--debug`: the loader opens a console showing the host. */
    var showConsole: Boolean by property(false)
}
