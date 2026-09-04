package dev.ancaria.idea.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.util.InheritanceUtil
import dev.ancaria.idea.Sacred
import dev.ancaria.idea.SacredBundle
import dev.ancaria.idea.SacredIcons
import org.jetbrains.uast.UDeclaration
import org.jetbrains.uast.UMethod
import javax.swing.Icon

/**
 * A method that hears about something the game did.
 *
 * The shape is the same one `Bus.register` looks for and the linter checks:
 * `@Subscribe`, one parameter, and that parameter an event type. A method with
 * the annotation and the wrong shape never fires, so it gets no icon -- which
 * is a quiet way of saying the thing the linter says loudly at build time.
 *
 * The event's name is in the tooltip and the icon navigates to it, because the
 * parameter type is the only place the event is named: there is no event id to
 * look up, no string to grep, and the way to find out what `Pickup` carries is
 * to read `Pickup`.
 */
class EventMarker : SacredMarker() {

    override fun getName(): String = SacredBundle.message("marker.event.name")

    override fun getIcon(): Icon = SacredIcons.Event

    override fun mark(declaration: UDeclaration, anchor: PsiElement): LineMarkerInfo<*>? {
        val method = (declaration as? UMethod)?.javaPsi ?: return null
        val subscribe = method.getAnnotation(Sacred.SUBSCRIBE) ?: return null
        val parameter = method.parameterList.parameters.singleOrNull() ?: return null
        val event = (parameter.type as? PsiClassType)?.resolve() ?: return null
        if (!isEvent(event)) return null

        val priority = subscribe.findAttributeValue("priority")?.text?.substringAfterLast('.')
        val tooltip = if (priority.isNullOrBlank() || priority == "NORMAL") {
            SacredBundle.message("marker.event.plain", event.name.orEmpty())
        } else {
            SacredBundle.message("marker.event.priority", event.name.orEmpty(), priority)
        }

        // MONITOR reads the event after everyone else and cannot change it, so
        // it gets the same mark in grey rather than the gold every other
        // listener earns -- a glance at the gutter already says which kind it is.
        val icon = if (priority == "MONITOR") SacredIcons.EventMonitor else SacredIcons.Event

        return marker(anchor, icon, tooltip) { event as? Navigatable }
    }

    /**
     * Everything under the event package except `Guard`, which is the one class
     * in there that does not extend `Event`.
     *
     * The hierarchy is the real answer and the package is the fallback: before
     * the first Gradle sync the API is not on the classpath, `Event` does not
     * resolve, and a marker that waits for an import to be resolvable is a
     * marker that is missing exactly when somebody is writing the listener.
     */
    private fun isEvent(klass: PsiClass): Boolean {
        if (InheritanceUtil.isInheritor(klass, Sacred.EVENT_BASE)) return true
        val qualified = klass.qualifiedName ?: return false
        return qualified.startsWith("${Sacred.EVENT_PACKAGE}.") &&
            qualified != "${Sacred.EVENT_PACKAGE}.Guard"
    }
}
