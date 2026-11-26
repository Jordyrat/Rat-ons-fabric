package com.ratons.features.misc.update

import com.google.gson.JsonParser
import com.ratons.Ratons
import com.ratons.config.RatConfig
import com.ratons.config.UpdateMode
import com.ratons.utils.ChatUtils
import com.ratons.utils.SemanticVersion
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * Checks GitHub releases at launch. A newer release is downloaded and handed to
 * [SelfUpdater], which swaps the jar once the game has closed; when the mod is not a plain
 * jar in a mods folder, the chat message links the download instead.
 */
object UpdateManager {

    private const val REPO = "Jordyrat/Rat-ons-fabric"
    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private var checked = false
    private var announced = false

    fun onTick() {
        if (checked || RatConfig.updateMode == UpdateMode.None) return
        if (net.minecraft.client.Minecraft.getInstance().player == null) return
        checked = true
        check(manual = false)
    }

    /** The explicit command, which checks even when the mode is off. */
    fun checkForUpdates() = check(manual = true)

    private fun check(manual: Boolean) {
        Thread.ofVirtual().name("ratons-update-check").start {
            runCatching { fetchLatestRelease() }
                .onSuccess { release ->
                    val current = SemanticVersion.of(Ratons.version)
                    if (SemanticVersion.of(release.tag) <= current) return@onSuccess
                    if (announced) return@onSuccess
                    announced = true
                    deliver(release, manual)
                }
                .onFailure { Ratons.logger.warn("Update check failed", it) }
        }
    }

    private fun deliver(release: Release, manual: Boolean) {
        val mode = RatConfig.updateMode
        if (mode == UpdateMode.None && !manual) return
        val staged = if (mode == UpdateMode.Automatic) {
            release.assetUrl?.let { SelfUpdater.stageUpdate(release.tag, it) }
        } else {
            null
        }
        net.minecraft.client.Minecraft.getInstance().execute {
            if (staged == null && mode == UpdateMode.Automatic && release.assetUrl != null) {
                ChatUtils.chat("§aVersion §e${release.tag}§a downloaded and will replace this jar when you close the game.")
                return@execute
            }
            ChatUtils.chat("§aVersion §e${release.tag}§a is available:")
            if (staged != null) Ratons.logger.info("Not self-updating: ${staged}")
            ChatUtils.openLink("§b${release.url}", release.url)
        }
    }

    private class Release(val tag: String, val url: String, val assetUrl: String?)

    private fun fetchLatestRelease(): Release {
        val request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/$REPO/releases/latest"))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Rat-ons")
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        require(response.statusCode() == 200) { "GitHub returned ${response.statusCode()}" }
        val json = JsonParser.parseString(response.body()).asJsonObject
        val asset = json.getAsJsonArray("assets")?.firstOrNull { it.asJsonObject["name"]?.asString?.endsWith(".jar") == true }
        return Release(
            tag = json.get("tag_name").asString,
            url = json.get("html_url").asString,
            assetUrl = asset?.asJsonObject?.get("browser_download_url")?.asString,
        )
    }
}
