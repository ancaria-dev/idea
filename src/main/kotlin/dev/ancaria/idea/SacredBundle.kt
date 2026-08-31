package dev.ancaria.idea

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.SacredBundle"

/**
 * Every string a person reads, in one file.
 *
 * Not a preference: `plugin.xml` names this bundle for the Settings page title,
 * and the marketplace's own tooling reads it, so the alternative is the same
 * words in two places. [PropertyKey] makes a key that is not in the file a
 * compile error rather than a `!key!` in the dialog.
 */
object SacredBundle : DynamicBundle(BUNDLE) {

    fun message(
        @PropertyKey(resourceBundle = BUNDLE) key: String,
        vararg params: Any,
    ): @Nls String = getMessage(key, *params)
}
