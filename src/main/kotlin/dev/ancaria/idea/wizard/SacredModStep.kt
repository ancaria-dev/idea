package dev.ancaria.idea.wizard

import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseStep
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import dev.ancaria.coderpack.templates.Fail
import dev.ancaria.coderpack.templates.Names
import dev.ancaria.idea.Sacred
import dev.ancaria.idea.SacredBundle
import java.nio.file.Path

/**
 * Everything the scaffolder needs that the platform's own two fields do not
 * already say.
 *
 * The defaults are the command line's, worked out by the same code: the project
 * name suggests a mod name, the mod name suggests an id, the group and the id
 * suggest a package. Each one stops following the one above it the moment
 * somebody types in it, which is what `dependsOn` does by default.
 *
 * Two of these answers are the reason this dialog is not four fields. Language
 * is what the mod is written in and reaches the player as bytecode. Build script
 * is what the project's own build is typed in and reaches nobody. They are
 * separate rows because they are separate questions, and the Groovy in one has
 * nothing to do with the Groovy in the other.
 */
class SacredModStep(private val base: NewProjectWizardBaseStep) :
    AbstractNewProjectWizardStep(base) {

    private val groupProperty = propertyGraph.lazyProperty { DEFAULT_GROUP }
    private val modNameProperty = propertyGraph.lazyProperty { suggestedName() }
    private val modIdProperty = propertyGraph.lazyProperty { Names.suggest(base.name) }
    private val descriptionProperty = propertyGraph.lazyProperty { "" }
    private val packageProperty = propertyGraph.lazyProperty { packageOf() }
    private val authorProperty = propertyGraph.lazyProperty { System.getProperty("user.name").orEmpty() }
    private val versionProperty = propertyGraph.lazyProperty { DEFAULT_VERSION }

    private val templateProperty = propertyGraph.lazyProperty { Catalog.defaultTemplate() }
    private val languageProperty =
        propertyGraph.lazyProperty { Catalog.defaultLanguage(Catalog.defaultTemplate().id) }
    private val dslProperty = propertyGraph.lazyProperty { Catalog.defaultDsl() }

    private val gitProperty = propertyGraph.lazyProperty { true }
    private val srmlProperty = propertyGraph.lazyProperty { true }
    private val repositoryProperty = propertyGraph.lazyProperty { "" }

    /** Rebuilt when the template changes, because a template offers what it has. */
    private val languages =
        MutableCollectionComboBoxModel(Catalog.languages(Catalog.defaultTemplate().id))

    init {
        modNameProperty.dependsOn(base.nameProperty) { suggestedName() }
        modIdProperty.dependsOn(modNameProperty) { Names.suggest(modNameProperty.get()) }
        packageProperty.dependsOn(modIdProperty) { packageOf() }
        packageProperty.dependsOn(groupProperty) { packageOf() }

        // Not a `dependsOn`: the model and the selection have to change in that
        // order, and two independent listeners on the same property do not
        // promise one. A language a template has no entrypoint for is not a
        // preference worth keeping either: it is a project that cannot be
        // written, so the choice survives only when the new template has it.
        templateProperty.afterChange { template ->
            val offered = Catalog.languages(template.id)
            val kept = offered.firstOrNull { it.id == languageProperty.get().id } ?: offered.first()
            languages.update(offered)
            languageProperty.set(kept)
        }
    }

    override fun setupUI(builder: Panel) {
        with(builder) {
            row(SacredBundle.message("wizard.group")) {
                textField()
                    .bindText(groupProperty)
                    .columns(COLUMNS_MEDIUM)
                    .validationOnInput { field -> packageProblem(field.text) }
            }

            row(SacredBundle.message("wizard.mod.name")) {
                textField()
                    .bindText(modNameProperty)
                    .columns(COLUMNS_MEDIUM)
                    .validationOnInput { field ->
                        if (field.text.isBlank()) {
                            error(SacredBundle.message("wizard.error.blank"))
                        } else {
                            null
                        }
                    }
            }.rowComment(SacredBundle.message("wizard.mod.name.comment"))

            row(SacredBundle.message("wizard.mod.id")) {
                textField()
                    .bindText(modIdProperty)
                    .columns(COLUMNS_MEDIUM)
                    .validationOnInput { field -> idProblem(field.text) }
            }.rowComment(SacredBundle.message("wizard.mod.id.comment"))

            row(SacredBundle.message("wizard.description")) {
                textField()
                    .bindText(descriptionProperty)
                    .align(AlignX.FILL)
            }

            separator()

            row(SacredBundle.message("wizard.template")) {
                comboBox(Catalog.templates()).bindItem(templateProperty)
            }.rowComment(SacredBundle.message("wizard.template.comment"))

            row(SacredBundle.message("wizard.language")) {
                comboBox(languages).bindItem(languageProperty)
            }.rowComment(SacredBundle.message("wizard.language.comment"))

            row(SacredBundle.message("wizard.dsl")) {
                comboBox(Catalog.dsls()).bindItem(dslProperty)
            }.rowComment(SacredBundle.message("wizard.dsl.comment"))

            separator()

            row {
                checkBox(SacredBundle.message("wizard.git")).bindSelected(gitProperty)
            }.rowComment(SacredBundle.message("wizard.git.comment"))

            row {
                checkBox(SacredBundle.message("wizard.srml")).bindSelected(srmlProperty)
            }.rowComment(SacredBundle.message("wizard.srml.comment"))

            row(SacredBundle.message("wizard.repository")) {
                textField()
                    .bindText(repositoryProperty)
                    .align(AlignX.FILL)
                    .enabledIf(srmlProperty)
            }.rowComment(SacredBundle.message("wizard.repository.comment"))

            collapsibleGroup(SacredBundle.message("wizard.advanced")) {
                row(SacredBundle.message("wizard.package")) {
                    textField()
                        .bindText(packageProperty)
                        .align(AlignX.FILL)
                        .validationOnInput { field -> packageProblem(field.text) }
                }
                row(SacredBundle.message("wizard.author")) {
                    textField().bindText(authorProperty).columns(COLUMNS_MEDIUM)
                }
                row(SacredBundle.message("wizard.version")) {
                    textField().bindText(versionProperty).columns(COLUMNS_MEDIUM)
                }
            }

            row {
                browserLink(SacredBundle.message("wizard.site"), Sacred.SITE)
            }
        }
    }

    override fun setupProject(project: Project) {
        val name = modNameProperty.get().trim()
        SacredProjectSetup.run(
            project,
            Scaffolding.Request(
                root = Path.of(base.path, base.name),
                id = modIdProperty.get().trim(),
                displayName = name,
                description = descriptionProperty.get().trim()
                    .ifEmpty { SacredBundle.message("wizard.description.default", name) },
                version = versionProperty.get().trim().ifEmpty { DEFAULT_VERSION },
                pkg = packageProperty.get().trim(),
                author = authorProperty.get().trim().ifEmpty { "unknown" },
                repository = repositoryProperty.get().trim().ifEmpty { null },
                template = templateProperty.get().id,
                language = languageProperty.get().id,
                dsl = dslProperty.get().id,
                srml = srmlProperty.get(),
                git = gitProperty.get(),
            ),
        )
    }

    /**
     * The group plus the id with its hyphens gone.
     *
     * The tail comes out of [Names.pkg] rather than out of a second `replace`
     * here, because that function also handles an id starting with a digit,
     * which is a legal mod id and not a legal package segment.
     */
    private fun packageOf(): String {
        val tail = Names.pkg(modIdProperty.get()).removePrefix("mods.")
        val group = groupProperty.get().trim()
        return if (group.isEmpty()) "mods.$tail" else "$group.$tail"
    }

    private fun suggestedName(): String = Names.display(Names.suggest(base.name))

    private fun ValidationInfoBuilder.packageProblem(text: String): ValidationInfo? = try {
        Names.validPkg(text)
        null
    } catch (refused: Fail) {
        error(refused.message.orEmpty())
    }

    /**
     * The id rule, asked of the code that owns it.
     *
     * `Names.id` is what `coderpack new` calls, and it asks `Ids` in the
     * linter, which is where everything the Gradle plugin, the scaffolder and
     * the loader all have to agree about lives. Calling it rather than
     * repeating the rule also means this dialog and the command line refuse the
     * same name with the same sentence.
     */
    private fun ValidationInfoBuilder.idProblem(text: String): ValidationInfo? = try {
        Names.id(text)
        null
    } catch (refused: Fail) {
        error(refused.message.orEmpty())
    }

    private companion object {
        const val DEFAULT_GROUP = "mods"
        const val DEFAULT_VERSION = "1.0.0"
    }
}
