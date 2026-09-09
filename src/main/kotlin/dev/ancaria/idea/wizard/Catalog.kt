package dev.ancaria.idea.wizard

import dev.ancaria.coderpack.templates.Dsls
import dev.ancaria.coderpack.templates.Languages
import dev.ancaria.coderpack.templates.Templates

/**
 * One row of a combo box: what the scaffolder calls it, and what a person reads.
 *
 * [toString] is what the combo renders, so the label and the description travel
 * together and the renderer stays a default one.
 */
data class Choice(val id: String, val label: String, val description: String) {
    override fun toString(): String = label
}

/**
 * The three lists the dialog offers, read out of the scaffolder rather than
 * typed here.
 *
 * That is the whole reason this plugin depends on `dev.ancaria.coderpack:
 * templates` instead of carrying its own copies: a template added over there is
 * in this dialog on the next build, and a language this plugin offered that the
 * scaffolder had never heard of would be a combo box entry that fails after the
 * Finish button.
 *
 * The labels are capitalised here and nowhere else. A directory name is
 * `kotlin`. A dialog says Kotlin.
 */
object Catalog {

    fun templates(): List<Choice> = Templates.names().map {
        Choice(it, it.replaceFirstChar(Char::uppercaseChar), Templates.describe(it))
    }

    /** Only the languages this template actually has an entrypoint written in. */
    fun languages(template: String): List<Choice> = Templates.languages(template).map {
        Choice(it, label(it), Languages.describe(it))
    }

    fun dsls(): List<Choice> = Dsls.names().map {
        Choice(it, Dsls.values(it)["buildFile"] ?: label(it), Dsls.describe(it))
    }

    fun defaultTemplate(): Choice = templates().first { it.id == Templates.DEFAULT }

    fun defaultLanguage(template: String): Choice =
        languages(template).firstOrNull { it.id == Languages.DEFAULT } ?: languages(template).first()

    fun defaultDsl(): Choice = dsls().first { it.id == Dsls.DEFAULT }

    private fun label(id: String) = when (id) {
        "java" -> "Java"
        "kotlin" -> "Kotlin"
        "groovy" -> "Groovy"
        else -> id.replaceFirstChar(Char::uppercaseChar)
    }
}
