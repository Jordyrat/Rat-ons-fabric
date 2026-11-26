package com.ratons.config

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ratons.Ratons
import com.ratons.utils.ErrorManager
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import com.teamresourceful.resourcefulconfig.api.loader.Configurator
import com.teamresourceful.resourcefulconfig.api.types.ResourcefulConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Owns the Resourceful Config registration for the mod, and carries the one-off migration from
 * the 1.8.9 build's moulconfig file at the same path.
 */
object ConfigManager {

    private val configurator = Configurator(Ratons.MOD_ID)
    private val path: Path = Path.of("config", Ratons.MOD_ID, "config.json")

    private lateinit var registered: ResourcefulConfig

    fun load() {
        val legacy = readLegacy()
        registered = RatConfig.register(configurator)
        if (legacy != null) applyLegacy(legacy)
    }

    fun save() {
        registered.save()
    }

    fun openScreen(parent: Screen?) {
        // The chat screen closes itself right after a command runs, so anything set here would
        // be replaced by null within the same call. Opening on the next frame survives that.
        val client = Minecraft.getInstance()
        client.execute {
            ErrorManager.guard("opening the config screen") {
                val builder = ResourcefulConfigScreen.make(registered)
                parent?.let(builder::withParent)
                client.gui.setScreen(builder.build())
            }
        }
    }

    private fun readLegacy(): JsonObject? {
        if (!Files.exists(path)) return null
        val raw = runCatching { JsonParser.parseString(Files.readString(path)).asJsonObject }.getOrNull() ?: return null
        if (!raw.has("instancesConfig") && !raw.has("about")) return null
        Ratons.logger.info("Migrating the 1.8.9 config")
        return raw
    }

    private fun applyLegacy(raw: JsonObject) {
        fun section(vararg keys: String): JsonObject? {
            var current: JsonObject = raw
            for (key in keys) {
                current = current.getAsJsonObject(key) ?: return null
            }
            return current
        }

        fun flag(vararg keys: String): Boolean? {
            val parent = if (keys.size == 1) raw else section(*keys.dropLast(1).toTypedArray()) ?: return null
            val value = parent.get(keys.last()) ?: return null
            return if (value.isJsonPrimitive) value.asBoolean else null
        }

        flag("about", "autoUpdates")?.let { RatConfig.autoUpdates = it }
        flag("about", "debug")?.let { RatConfig.debug = it }
        flag("instancesConfig", "dungeonsCategory", "relicSpawnTimer")?.let { DungeonsConfig.relicSpawnTimer = it }
        flag("instancesConfig", "dungeonsCategory", "closeSecretChest")?.let { DungeonsConfig.closeSecretChest = it }
        flag("instancesConfig", "autoRefill", "enabled")?.let { AutoRefillConfig.enabled = it }
        flag("instancesConfig", "autoRefill", "refillPearls")?.let { AutoRefillConfig.pearls = it }
        flag("instancesConfig", "autoRefill", "refillDecoys")?.let { AutoRefillConfig.decoys = it }
        flag("instancesConfig", "autoRefill", "refillJerrys")?.let { AutoRefillConfig.jerrys = it }
        flag("instancesConfig", "partyFinder", "partySizeDisplay")?.let { PartyFinderConfig.partySizeDisplay = it }
        flag("misc", "dungeonItemData")?.let { MiscConfig.dungeonItemData = it }
        flag("misc", "itemAgeData")?.let { MiscConfig.itemAgeData = it }
        save()
        // Resourceful Config writes config.jsonc, so the old file has to be moved out of the
        // way. Leaving it there would re-run this migration on every launch and overwrite
        // whatever the player has changed since.
        // Resourceful Config sometimes consumes the old file itself when it converts to
        // .jsonc, so a missing source here is success, not a failure.
        if (Files.exists(path)) {
            runCatching {
                Files.move(path, path.resolveSibling("config.json.legacy"), StandardCopyOption.REPLACE_EXISTING)
            }.onSuccess {
                Ratons.logger.info("Migrated the 1.8.9 config; the original is now config.json.legacy")
            }.onFailure { Ratons.logger.warn("Could not set aside the legacy config", it) }
        }
    }
}
