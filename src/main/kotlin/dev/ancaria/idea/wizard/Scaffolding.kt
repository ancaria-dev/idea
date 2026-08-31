package dev.ancaria.idea.wizard

import dev.ancaria.coderpack.templates.Git
import dev.ancaria.coderpack.templates.Names
import dev.ancaria.coderpack.templates.New
import dev.ancaria.coderpack.templates.Scaffold
import dev.ancaria.coderpack.templates.Versions
import java.nio.file.Path

/**
 * The dialog's answers, handed to the scaffolder that the command line uses.
 *
 * Nothing here renders a template or knows what a project contains. That is the
 * point: `coderpack new my-mod` and File | New Project write the same bytes,
 * because both call [Scaffold.plan] with the same map, and a template added in
 * ancaria-dev/build turns up in this dialog without a line changing here.
 *
 * The keys of that map are the contract between the two, and they are the same
 * eleven `New` fills in. A twelfth added there and not here is a placeholder
 * nothing fills in, which the renderer refuses rather than writing through.
 */
object Scaffolding {

    data class Request(
        val root: Path,
        val id: String,
        val displayName: String,
        val description: String,
        val version: String,
        val pkg: String,
        val author: String,
        /** Where the project will live. Only ever read when [srml] is on. */
        val repository: String?,
        val template: String,
        val language: String,
        val dsl: String,
        val srml: Boolean,
        val git: Boolean,
    )

    /** What was written, and what happened to git. Both belong in the log. */
    data class Written(val paths: Set<String>, val git: String?)

    fun write(request: Request): Written {
        val klass = Names.klass(request.id)
        val values = mapOf(
            "id" to request.id,
            "name" to request.displayName,
            "description" to request.description,
            "version" to request.version,
            "package" to request.pkg,
            "packagePath" to request.pkg.replace('.', '/'),
            "class" to klass,
            "entrypoint" to "${request.pkg}.$klass",
            "author" to request.author,
            "repo" to New.repository(request.id, request.repository),
            // Written by the scaffolder's own build, so this plugin cannot name
            // a plugin version or an API version that was never published.
            "plugin" to Versions.plugin,
            "api" to Versions.api,
        )

        // Rendered whole before the first byte, the way the command line does
        // it, so a template that asks for something nobody filled in fails with
        // an empty project directory rather than half a project in it.
        val files = Scaffold.plan(
            request.template,
            request.language,
            values,
            request.dsl,
            request.srml,
        )
        // Forced, unlike on the command line: the IDE has already made the
        // directory and put a .idea in it by the time this runs, so "not empty"
        // is the normal case here rather than the dangerous one.
        Scaffold.write(request.root, files, force = true)

        val git = if (request.git) {
            Git.init(request.root.toFile(), New.repository(request.id, request.repository))
        } else {
            null
        }
        return Written(files.keys, git)
    }
}
