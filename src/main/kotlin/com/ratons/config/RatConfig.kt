package com.ratons.config

import com.google.gson.GsonBuilder
import com.ratons.Ratons
import java.nio.file.Files
import java.nio.file.Path

/**
 * Replaces SkyHanni's managed moulconfig. Kept deliberately plain: Gson on a tree of
 * data classes, saved on change and on shutdown, edited with `/rat set <path> <value>`.
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
        config = try {
            if (Files.exists(path)) gson.fromJson(Files.readString(path), RatConfig::class.java) else RatConfig()
        } catch (e: Exception) {
            Ratons.logger.warn("Could not read $path, starting from defaults", e)
            RatConfig()
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
}
