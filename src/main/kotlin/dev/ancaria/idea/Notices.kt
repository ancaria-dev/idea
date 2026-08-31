package dev.ancaria.idea

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * The balloon group declared in `plugin.xml`, reached through one function
 * rather than four lookups.
 *
 * Everything this plugin has to say after a dialog has closed goes here: a
 * scaffolder that refused a name, a launcher release that would not download, a
 * game that did not start. None of it is worth a modal dialog and all of it is
 * worth the Notifications log, which is where a balloon ends up.
 */
object Notices {

    fun info(project: Project?, title: String, content: String) =
        show(project, title, content, NotificationType.INFORMATION)

    fun warn(project: Project?, title: String, content: String) =
        show(project, title, content, NotificationType.WARNING)

    fun error(project: Project?, title: String, content: String) =
        show(project, title, content, NotificationType.ERROR)

    private fun show(project: Project?, title: String, content: String, type: NotificationType) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(Sacred.NOTIFICATIONS)
            .createNotification(title, content, type)
            .notify(project)
    }
}
