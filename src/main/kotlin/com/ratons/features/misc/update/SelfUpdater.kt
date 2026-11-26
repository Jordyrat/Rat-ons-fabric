package com.ratons.features.misc.update

import com.ratons.Ratons
import net.fabricmc.loader.api.FabricLoader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.zip.ZipFile

/**
 * Replaces this mod's own jar with a downloaded release.
 *
 * Windows and macOS both keep the loaded jar open for the life of the process, so the swap cannot
 * happen while the game runs. The downloaded jar is parked beside the old one as a `.pending` file
 * and a small helper script waits for this process to exit before moving it into place, which is
 * the only moment the file is writable.
 */
object SelfUpdater {

    private val http: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    private val isWindows = System.getProperty("os.name").lowercase().contains("win")

    /**
     * The jar this client loaded, or null when the mod is not a plain file in a mods folder —
     * a development client runs from build output, and a launcher-managed install may keep mods
     * somewhere we should not write to.
     */
    fun ownJar(): Path? = runCatching {
        val loader = FabricLoader.getInstance()
        if (loader.isDevelopmentEnvironment) return null
        val container = loader.getModContainer(Ratons.MOD_ID).orElse(null) ?: return null
        // A jar mod's root path is the root of its own zip filesystem rather than the file,
        // so the origin paths are what name the jar on disk.
        val paths = runCatching { container.origin.paths }.getOrDefault(emptyList()) +
            runCatching { container.rootPaths }.getOrDefault(emptyList())
        val jar = paths
            .filter { it.fileName?.toString()?.endsWith(".jar") == true && Files.isRegularFile(it) }
            .distinct()
            .singleOrNull() ?: return null
        jar.takeIf { it.parent?.fileName?.toString() == "mods" }
    }.getOrNull()

    /**
     * Downloads [assetUrl] and schedules the swap for when the game exits.
     * Returns null on success, or the reason it could not be staged.
     */
    fun stageUpdate(version: String, assetUrl: String): String? {
        val current = ownJar() ?: return "this is not a jar install"
        val pending = current.resolveSibling("ratons-$version.jar.pending")
        val failure = runCatching { download(version, assetUrl, current, pending) }.exceptionOrNull()
        if (failure == null) return null
        Ratons.logger.warn("Could not stage the $version update", failure)
        runCatching { Files.deleteIfExists(pending) }
        return failure.message ?: failure.javaClass.simpleName
    }

    private fun download(version: String, assetUrl: String, current: Path, pending: Path) {
        val request = HttpRequest.newBuilder(URI.create(assetUrl))
            .header("User-Agent", "Rat-ons")
            .timeout(Duration.ofMinutes(2))
            .GET()
            .build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofFile(pending))
        require(response.statusCode() == 200) { "GitHub returned ${response.statusCode()}" }
        require(isRatonsJar(pending)) { "the download is not a Rat-ons jar" }
        val reached = current.resolveSibling("ratons-$version.jar")
        val script = writeHelper(current, pending, reached)
        ProcessBuilder(script.toString()).redirectErrorStream(true).start()
        Ratons.logger.info("Staged $version at $pending; it replaces $current when the game closes")
    }

    private fun isRatonsJar(file: Path): Boolean = runCatching {
        ZipFile(file.toFile()).use { zip ->
            val entry = zip.getEntry("fabric.mod.json") ?: return false
            zip.getInputStream(entry).bufferedReader().use { it.readText().contains("\"ratons\"") }
        }
    }.getOrDefault(false)

    private fun writeHelper(current: Path, pending: Path, target: Path): Path {
        val pid = ProcessHandle.current().pid()
        val script = Files.createTempFile("ratons-update", if (isWindows) ".cmd" else ".sh")
        val body = if (isWindows) {
            """
            @echo off
            :wait
            tasklist /FI "PID eq $pid" 2>nul | findstr /R /C:" $pid " >nul
            if not errorlevel 1 (
              ping -n 3 127.0.0.1 >nul
              goto wait
            )
            move /Y "$pending" "$target" >nul 2>&1
            set /a tries=0
            :remove
            if exist "$current" if %tries% lss 30 (
              del "$current" >nul 2>&1
              set /a tries+=1
              ping -n 2 127.0.0.1 >nul
              goto remove
            )
            del "%~f0" >nul 2>&1
            """.trimIndent().replace("\n", "\r\n")
        } else {
            """
            #!/bin/sh
            while kill -0 $pid 2>/dev/null; do sleep 2; done
            mv -f "$pending" "$target"
            for _ in $(seq 30); do [ -e "$current" ] || break; rm -f "$current"; sleep 1; done
            rm -f "${'$'}0"
            """.trimIndent()
        }
        Files.writeString(script, body)
        if (!isWindows) script.toFile().setExecutable(true)
        return script
    }
}
