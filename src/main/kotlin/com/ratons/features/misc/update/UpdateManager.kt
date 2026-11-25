package com.ratons.features.misc.update

import com.google.gson.JsonParser
import com.ratons.Ratons
import com.ratons.config.ConfigManager
import com.ratons.utils.ChatUtils
import com.ratons.utils.SemanticVersion
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

/**
 * SkyHanni's libautoupdate swapped a jar on disk. Fabric loads mods from the mods folder
 * at launch, so this port checks GitHub releases and points the player at the download.
 */
object UpdateManager {

    private const val REPO = "Jordyrat/Rat-ons"
    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private var checked = false
    private var announced = false

    fun onTick() {
        if (checked || !ConfigManager.config.autoUpdates) return
        if (net.minecraft.client.Minecraft.getInstance().player == null) return
        checked = true
        checkForUpdates()
    }

    fun checkForUpdates() {
        Thread.ofVirtual().name("ratons-update-check").start {
            runCatching { fetchLatestRelease() }
                .onSuccess { (tag, url) ->
                    val current = SemanticVersion.of(Ratons.version)
                    val latest = SemanticVersion.of(tag)
                    if (latest <= current) return@onSuccess
                    if (announced) return@onSuccess
                    announced = true
                    net.minecraft.client.Minecraft.getInstance().execute {
                        ChatUtils.chat("§aVersion §e$tag§a is available:")
                        ChatUtils.openLink("§b$url", url)
                    }
                }
                .onFailure { Ratons.logger.warn("Update check failed", it) }
        }
    }

    private fun fetchLatestRelease(): Pair<String, String> {
        val request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/$REPO/releases/latest"))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "Rat-ons")
            .timeout(Duration.ofSeconds(15))
            .GET()
            .build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        require(response.statusCode() == 200) { "GitHub returned ${response.statusCode()}" }
        val json = JsonParser.parseString(response.body()).asJsonObject
        return json.get("tag_name").asString to json.get("html_url").asString
    }
}
