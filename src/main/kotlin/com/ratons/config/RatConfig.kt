package com.ratons.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ratons.Ratons
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Replaces SkyHanni's managed moulconfig. Kept deliberately plain: Gson on a tree of
 * data classes, saved on change and on shutdown, edited with `/rat toggle <feature>`.
 */
data class RatConfig(
    val autoUpdates: Boolean = true,
    val debug: Boolean = false,
    val dungeons: DungeonsConfig = DungeonsConfig(),
    val autoRefill: AutoRefillConfig = AutoRefillConfig(),
    val partyFinder: PartyFinderConfig = PartyFinderConfig(),
    val misc: MiscConfig = MiscConfig(),
)

data class DungeonsConfig(
    val relicSpawnTimer: Boolean = false,
    val closeSecretChest: Boolean = false,
)

data class AutoRefillConfig(
    val enabled: Boolean = false,
    val pearls: Boolean = false,
    val decoys: Boolean = false,
    val jerrys: Boolean = false,
)

data class PartyFinderConfig(
    val partySizeDisplay: Boolean = false,
)

data class MiscConfig(
    val dungeonItemData: Boolean = false,
    val itemAgeData: Boolean = false,
)

object ConfigManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val path: Path = Path.of("config", Ratons.MOD_ID, "config.json")

    var config: RatConfig = RatConfig()
        private set

    fun load() {
        if (!Files.exists(path)) {
            config = RatConfig()
            save()
            return
        }
        val raw = runCatching { JsonParser.parseString(Files.readString(path)).asJsonObject }.getOrNull()
        config = when {
            raw == null -> RatConfig()
            isLegacy(raw) -> migrate(raw)
            else -> runCatching { gson.fromJson(raw, RatConfig::class.java) }.getOrDefault(RatConfig())
        }
        save()
    }

    fun save() {
        try {
            Files.createDirectories(path.parent)
            Files.writeString(path, gson.toJson(config))
        } catch (e: Exception) {
            Ratons.logger.warn("Could not save $path", e)
        }
    }

    fun update(transform: (RatConfig) -> RatConfig) {
        config = transform(config)
        save()
    }

    /**
     * The 1.8.9 build stored the same file under moulconfig's shape (about, instancesConfig).
     * Reading that as RatConfig would silently drop every value, so convert it instead and
     * keep the original beside the new one.
     */
    private fun isLegacy(raw: JsonObject) = raw.has("instancesConfig") || raw.has("about")

    private fun migrate(raw: JsonObject): RatConfig {
        runCatching {
            Files.copy(path, path.resolveSibling("config.json.legacy"), StandardCopyOption.REPLACE_EXISTING)
        }.onFailure { Ratons.logger.warn("Could not back up the legacy config", it) }

        fun section(vararg keys: String): JsonObject? {
            var current: JsonObject = raw
            for (key in keys) {
                current = current.getAsJsonObject(key) ?: return null
            }
            return current
        }

        fun flag(vararg path: String): Boolean? {
            val parent = if (path.size == 1) raw else section(*path.dropLast(1).toTypedArray()) ?: return null
            val value = parent.get(path.last()) ?: return null
            return if (value.isJsonPrimitive) value.asBoolean else null
        }

        val migrated = RatConfig(
            autoUpdates = flag("about", "autoUpdates") ?: true,
            debug = flag("about", "debug") ?: false,
            dungeons = DungeonsConfig(
                relicSpawnTimer = flag("instancesConfig", "dungeonsCategory", "relicSpawnTimer") ?: false,
                closeSecretChest = flag("instancesConfig", "dungeonsCategory", "closeSecretChest") ?: false,
            ),
            autoRefill = AutoRefillConfig(
                enabled = flag("instancesConfig", "autoRefill", "enabled") ?: false,
                pearls = flag("instancesConfig", "autoRefill", "refillPearls") ?: false,
                decoys = flag("instancesConfig", "autoRefill", "refillDecoys") ?: false,
                jerrys = flag("instancesConfig", "autoRefill", "refillJerrys") ?: false,
            ),
            partyFinder = PartyFinderConfig(
                partySizeDisplay = flag("instancesConfig", "partyFinder", "partySizeDisplay") ?: false,
            ),
            misc = MiscConfig(
                dungeonItemData = flag("misc", "dungeonItemData") ?: false,
                itemAgeData = flag("misc", "itemAgeData") ?: false,
            ),
        )
        Ratons.logger.info("Migrated the 1.8.9 config, original kept as config.json.legacy")
        return migrated
    }
}
