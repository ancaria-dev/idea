package dev.ancaria.idea.launcher

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.io.HttpRequests
import dev.ancaria.idea.Sacred
import java.io.IOException

/** One published Sacred Mod Loader, and where its executable is. */
data class LauncherRelease(
    /** `0.1.20`. What the settings dialog and the cache directory call it. */
    val version: String,
    /** `v0.1.20`. What the tag is, which is the only reason to keep it. */
    val tag: String,
    val url: String,
    val size: Long,
)

/**
 * What the launcher repository has published, asked of GitHub.
 *
 * The repository is public, so this is one unauthenticated GET and there is no
 * token to configure. The rate limit for those is sixty an hour per address,
 * which is why the answer is kept for the session: a settings dialog opened
 * five times should cost one request, not five.
 *
 * Nothing here throws. A machine with no network, or an hour that ran out of
 * requests, gets an empty list, and the dialog then offers whatever is
 * already in the cache, which is the honest answer rather than an error over a
 * list somebody may not need.
 */
object LauncherReleases {

    private val LOG = logger<LauncherReleases>()

    private const val ENDPOINT =
        "https://api.github.com/repos/${Sacred.LAUNCHER_REPO}/releases?per_page=50"

    @Volatile
    private var cached: List<LauncherRelease>? = null

    /** Newest first, as GitHub returns them. */
    fun list(refresh: Boolean = false): List<LauncherRelease> {
        if (!refresh) {
            cached?.let { return it }
        }
        val fetched = fetch()
        if (fetched.isNotEmpty() || cached == null) {
            cached = fetched
        }
        return cached.orEmpty()
    }

    fun latest(): LauncherRelease? = list().firstOrNull()

    fun find(version: String): LauncherRelease? = list().firstOrNull { it.version == version }

    private fun fetch(): List<LauncherRelease> = try {
        parse(
            HttpRequests.request(ENDPOINT)
                .accept("application/vnd.github+json")
                .productNameAsUserAgent()
                .readString()
        )
    } catch (offline: IOException) {
        LOG.info("could not list the launcher releases: ${offline.message}")
        emptyList()
    } catch (malformed: RuntimeException) {
        // Gson throws unchecked, and a body that is not the array we expect is
        // not worth a stack trace in somebody's IDE log every hour.
        LOG.info("could not read the launcher release list: ${malformed.message}")
        emptyList()
    }

    private fun parse(body: String): List<LauncherRelease> =
        JsonParser.parseString(body).asJsonArray.mapNotNull { element ->
            val release = element.asJsonObject
            if (release["draft"]?.asBoolean == true) return@mapNotNull null
            if (release["prerelease"]?.asBoolean == true) return@mapNotNull null
            val tag = release["tag_name"]?.asString ?: return@mapNotNull null
            // A release with no executable attached is a release nobody can run.
            // The workflow in `launcher` attaches exactly one file, under this
            // name. Anything else is a tag somebody made by hand.
            val asset = release["assets"]?.asJsonArray
                ?.map { it.asJsonObject }
                ?.firstOrNull { it["name"]?.asString == Sacred.LAUNCHER_FILE }
                ?: return@mapNotNull null
            val url = asset["browser_download_url"]?.asString ?: return@mapNotNull null
            LauncherRelease(
                version = tag.removePrefix("v"),
                tag = tag,
                url = url,
                size = asset["size"]?.asLong ?: 0L,
            )
        }
}
