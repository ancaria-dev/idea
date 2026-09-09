package dev.ancaria.idea

import dev.ancaria.coderpack.templates.Dsls
import dev.ancaria.coderpack.templates.Languages
import dev.ancaria.coderpack.templates.Templates
import dev.ancaria.idea.wizard.Catalog
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The three combo boxes, against the scaffolder they are read out of.
 *
 * The thing worth asserting is not that the lists have particular entries in
 * them (that is the scaffolder's business and it changes when somebody adds a
 * directory over there) but that this plugin offers exactly what the
 * scaffolder will accept. An entry in a dialog that fails after Finish is the
 * failure mode this whole arrangement exists to prevent.
 */
class CatalogTest {

    @Test
    fun `offers every template, language and build script the scaffolder has`() {
        assertEquals(Templates.names(), Catalog.templates().map { it.id })
        assertEquals(Dsls.names(), Catalog.dsls().map { it.id })
        for (template in Templates.names()) {
            assertEquals(
                Templates.languages(template),
                Catalog.languages(template).map { it.id },
                template,
            )
        }
    }

    @Test
    fun `every default is one of the rows it will be selected in`() {
        val template = Catalog.defaultTemplate()
        assertTrue(template in Catalog.templates(), template.toString())
        assertTrue(Catalog.defaultDsl() in Catalog.dsls())
        for (offered in Catalog.templates()) {
            val language = Catalog.defaultLanguage(offered.id)
            assertTrue(language in Catalog.languages(offered.id), "${offered.id}/$language")
        }
    }

    @Test
    fun `labels a build script with the file it writes`() {
        // `build.gradle.kts` rather than `Kotlin`, because the next row up
        // already says Kotlin and means something else entirely.
        val labels = Catalog.dsls().map { it.label }
        assertTrue("build.gradle.kts" in labels, labels.toString())
        assertTrue("build.gradle" in labels, labels.toString())
    }

    @Test
    fun `carries the description the scaffolder wrote`() {
        for (row in Catalog.languages(Templates.DEFAULT)) {
            assertEquals(Languages.describe(row.id), row.description)
            assertTrue(row.description.isNotBlank(), row.id)
        }
    }
}
