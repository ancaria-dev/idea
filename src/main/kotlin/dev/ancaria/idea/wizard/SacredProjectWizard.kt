package dev.ancaria.idea.wizard

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.ide.wizard.GeneratorNewProjectWizard
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.ide.wizard.NewProjectWizardChainStep.Companion.nextStep
import com.intellij.ide.wizard.NewProjectWizardStep
import com.intellij.ide.wizard.RootNewProjectWizardStep
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.SacredIcons
import javax.swing.Icon

/**
 * File | New | Project | Sacred Mod.
 *
 * A generator rather than a language: a Sacred mod is a Gradle project written
 * in one of three languages, so it belongs beside the framework generators and
 * not in the language list, where it would have to claim to be a fourth.
 *
 * The step chain is the platform's own name and location fields followed by
 * [SacredModStep]. Both are laid out on the same page, which is what
 * `nextStep` does: one screen, and Finish is the only button.
 */
class SacredProjectWizard : GeneratorNewProjectWizard {

    override val id: String = "SacredMod"

    override val name: String = SacredBundle.message("wizard.name")

    override val icon: Icon = SacredIcons.Sacred

    override val description: String = SacredBundle.message("wizard.subtitle")

    /**
     * High enough to sit under the languages and the IDE's own generators
     * rather than above them. This is somebody's second project, not their
     * first, and a generator that pushes Java down the list has misjudged that.
     */
    override val ordinal: Int = 900

    override fun createStep(context: WizardContext): NewProjectWizardStep =
        RootNewProjectWizardStep(context)
            .nextStep(::NewProjectWizardBaseStep)
            .nextStep(::SacredModStep)
}
