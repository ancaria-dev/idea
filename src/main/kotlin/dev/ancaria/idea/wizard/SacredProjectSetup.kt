package dev.ancaria.idea.wizard

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import dev.ancaria.coderpack.templates.Fail
import dev.ancaria.idea.Notices
import dev.ancaria.idea.SacredBundle

/**
 * What happens between Finish and a project on disk.
 *
 * Two things, and only two. The files go down, and the virtual file system is
 * told to look again, because it has no idea a tree appeared underneath it.
 *
 * Linking the Gradle build is deliberately not here. It has to happen after the
 * project window is open, and the platform's way of waiting for that from
 * inside project creation is marked internal -- so instead the startup activity
 * that gives a Sacred project its Run Sacred configuration links the build too.
 * It runs after the window opens by definition, it already asks the question
 * this would have to ask, and a project cloned from git rather than created
 * here goes through exactly the same path.
 */
object SacredProjectSetup {

    private val LOG = logger<SacredProjectSetup>()

    fun run(project: Project, request: Scaffolding.Request) {
        val written = try {
            Scaffolding.write(request)
        } catch (refused: Fail) {
            // Everything the scaffolder refuses, it refuses before writing a
            // byte, so there is nothing to clean up and the message is the one
            // the command line would have printed.
            LOG.warn("the scaffolder refused this project", refused)
            Notices.error(
                project,
                SacredBundle.message("wizard.failed"),
                refused.message.orEmpty(),
            )
            return
        }

        LOG.info("wrote ${written.paths.size} files into ${request.root}")
        written.git?.let { LOG.info(it) }

        // Synchronously, because the startup activity that links the Gradle
        // build asks the VFS whether this is a Sacred project, and it may run
        // very shortly after this returns.
        val root = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(request.root)
        if (root != null) {
            VfsUtil.markDirtyAndRefresh(false, true, true, root)
        }
    }
}
