package dev.ancaria.idea.markers

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.toUElement
import java.util.function.Supplier
import javax.swing.Icon

/**
 * A gutter icon on a declaration, in whichever of the three languages a mod is
 * written in.
 *
 * One implementation for Java, Kotlin and Groovy rather than three, because
 * everything these markers ask -- does this class implement that interface,
 * does this method carry that annotation, what is its one parameter -- is a
 * question UAST answers the same way in all of them. The registrations differ
 * per language and live in `plugin.xml`; the code does not.
 *
 * Two rules the platform enforces and this class keeps subclasses out of. A
 * line marker provider is called once per leaf token and must return a marker
 * anchored on a leaf, or the icon lands on the wrong line and the platform
 * logs about it. And a declaration has many leaves, so only the one UAST calls
 * the anchor -- the identifier -- may answer, or the same icon is drawn once
 * per token in the signature.
 */
abstract class SacredMarker : LineMarkerProviderDescriptor() {

    final override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element.firstChild != null) return null
        val declaration = element.parent?.toUElement() as? UDeclaration ?: return null
        if (declaration.uastAnchor?.sourcePsi !== element) return null
        return mark(declaration, element)
    }

    protected abstract fun mark(declaration: UDeclaration, anchor: PsiElement): LineMarkerInfo<*>?

    /**
     * @param target where clicking the icon goes, worked out lazily: resolving
     *   it while the marker is being built would resolve it for every method in
     *   the file whether anybody clicks or not.
     */
    protected fun marker(
        anchor: PsiElement,
        icon: Icon,
        tooltip: String,
        target: () -> Navigatable?,
    ): LineMarkerInfo<PsiElement> {
        val navigate = GutterIconNavigationHandler<PsiElement> { _, _ ->
            target()?.navigate(true)
        }
        return LineMarkerInfo(
            anchor,
            anchor.textRange,
            icon,
            { tooltip },
            navigate,
            GutterIconRenderer.Alignment.LEFT,
            Supplier { tooltip },
        )
    }
}
