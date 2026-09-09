package dev.ancaria.idea.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiModifier
import com.intellij.psi.util.InheritanceUtil
import dev.ancaria.idea.Sacred
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.SacredIcons
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UDeclaration
import javax.swing.Icon

/**
 * The class the mod starts in.
 *
 * Two sources, and neither is enough alone. The PSI says a class implements
 * `SacredMod`, which several classes in a project may do. The descriptor in the
 * build script says which one the loader will instantiate, and it names a
 * string that may not be a class at all. So the icon appears on any class the
 * loader could start, and the tooltip says which of the three situations this
 * one is in: the declared entrypoint, a candidate the descriptor does not
 * name, or a mod project whose build script has not been read yet.
 *
 * That middle case is the one worth having. A class that implements `SacredMod`
 * and is not the entrypoint is silently never loaded, and the first symptom is
 * a mod that starts and does nothing.
 */
class EntrypointMarker : SacredMarker() {

    override fun getName(): String = SacredBundle.message("marker.entrypoint.name")

    override fun getIcon(): Icon = SacredIcons.Entrypoint

    override fun mark(declaration: UDeclaration, anchor: PsiElement): LineMarkerInfo<*>? {
        val klass = (declaration as? UClass)?.javaPsi ?: return null
        // The loader calls getDeclaredConstructor().newInstance() on it, so an
        // interface or an abstract base is not a candidate however it is
        // declared, and the linter refuses one for the same reason.
        if (klass.isInterface || klass.hasModifierProperty(PsiModifier.ABSTRACT)) return null
        if (!InheritanceUtil.isInheritor(klass, Sacred.MOD_INTERFACE)) return null

        val declared = ModDescriptor.declaration(anchor)
        val tooltip = when {
            declared == null -> SacredBundle.message("marker.entrypoint.plain")
            declared.entrypoint == klass.qualifiedName ->
                SacredBundle.message("marker.entrypoint.declared", declared.file.name)
            else -> SacredBundle.message("marker.entrypoint.other", declared.entrypoint)
        }

        return marker(anchor, SacredIcons.Entrypoint, tooltip) {
            declared?.file?.virtualFile?.let {
                OpenFileDescriptor(anchor.project, it, declared.offset)
            }
        }
    }
}
