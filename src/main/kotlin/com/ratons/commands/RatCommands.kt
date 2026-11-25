package com.ratons.commands

import com.ratons.Ratons
import com.ratons.config.ConfigManager
import com.ratons.features.misc.update.UpdateManager
import com.ratons.utils.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal

/**
 * SkyHanni registered client commands through its own builder. Fabric's client command
 * registration is the direct replacement.
 */
object RatCommands {

    private val toggles: Map<String, Pair<String, (Boolean) -> Unit>> = mapOf(
        "relictimer" to ("Dungeons > Relic Spawn Timer" to { on ->
            ConfigManager.update { it.copy(dungeons = it.dungeons.copy(relicSpawnTimer = on)) }
        }),
        "closechest" to ("Dungeons > Close Secret Chest" to { on ->
            ConfigManager.update { it.copy(dungeons = it.dungeons.copy(closeSecretChest = on)) }
        }),
        "autorefill" to ("Instances > Auto Refill" to { on ->
            ConfigManager.update { it.copy(autoRefill = it.autoRefill.copy(enabled = on)) }
        }),
        "partyfinder" to ("Instances > Party Size Display" to { on ->
            ConfigManager.update { it.copy(partyFinder = it.partyFinder.copy(partySizeDisplay = on)) }
        }),
        "dungeonitemdata" to ("Misc > Dungeon Item Data" to { on ->
            ConfigManager.update { it.copy(misc = it.misc.copy(dungeonItemData = on)) }
        }),
        "itemagedata" to ("Misc > Item Age Data" to { on ->
            ConfigManager.update { it.copy(misc = it.misc.copy(itemAgeData = on)) }
        }),
        "autoupdates" to ("About > Auto Updates" to { on ->
            ConfigManager.update { it.copy(autoUpdates = on) }
        }),
        "debug" to ("About > Debug" to { on ->
            ConfigManager.update { it.copy(debug = on) }
        }),
    )

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("rat").executes {
                    listFeatures()
                    1
                }.then(
                    literal("status").executes {
                        status()
                        1
                    },
                ).then(
                    literal("toggle").then(
                        argument("feature", com.mojang.brigadier.arguments.StringArgumentType.word()).suggests { _, builder ->
                            toggles.keys.filter { it.startsWith(builder.remaining.lowercase()) }.forEach(builder::suggest)
                            builder.buildFuture()
                        }.executes { context ->
                            toggle(com.mojang.brigadier.arguments.StringArgumentType.getString(context, "feature"))
                            1
                        },
                    ),
                ).then(
                    literal("update").executes {
                        UpdateManager.checkForUpdates()
                        ChatUtils.chat("Checking for updates...")
                        1
                    },
                ),
            )

            dispatcher.register(
                literal("rathelp").executes {
                    ChatUtils.chat("§eCommands:")
                    ChatUtils.clickable("§7 - /rat §8- feature list", "/rat")
                    ChatUtils.clickable("§7 - /rat toggle <feature> §8- flip a feature", "/rat toggle ")
                    ChatUtils.clickable("§7 - /rat status §8- what Rat-ons sees", "/rat status")
                    ChatUtils.clickable("§7 - /rat update §8- check for a new release", "/rat update")
                    1
                },
            )
        }
    }

    private fun listFeatures() {
        ChatUtils.chat("§e${Ratons.MOD_NAME} ${Ratons.version} §7features:")
        toggles.forEach { (name, entry) ->
            val enabled = isEnabled(name)
            val state = if (enabled) "§aenabled" else "§7disabled"
            ChatUtils.clickable("§7 - §f$name §8(${entry.first}) §8- $state", "/rat toggle $name")
        }
    }

    private fun status() {
        ChatUtils.chat("§eRat-ons status:")
        ChatUtils.chat("§7 On SkyBlock: §f${com.ratons.data.SkyBlockState.onSkyBlock}")
        ChatUtils.chat("§7 Area: §f${com.ratons.data.SkyBlockState.area ?: "unknown"}")
        ChatUtils.chat("§7 Dungeon floor: §f${com.ratons.data.SkyBlockState.floor ?: "none"}")
        ChatUtils.chat("§7 Class: §f${com.ratons.data.SkyBlockState.playerClass?.name ?: "unknown"}")
    }

    private fun toggle(feature: String) {
        val key = feature.lowercase()
        val entry = toggles[key] ?: run {
            ChatUtils.userError("Unknown feature '$feature'. Try /rat for the list.")
            return
        }
        val now = !isEnabled(key)
        entry.second(now)
        ChatUtils.chat("§a${entry.first} is now ${if (now) "enabled" else "disabled"}.")
    }

    private fun isEnabled(feature: String): Boolean {
        val config = ConfigManager.config
        return when (feature) {
            "relictimer" -> config.dungeons.relicSpawnTimer
            "closechest" -> config.dungeons.closeSecretChest
            "autorefill" -> config.autoRefill.enabled
            "partyfinder" -> config.partyFinder.partySizeDisplay
            "dungeonitemdata" -> config.misc.dungeonItemData
            "itemagedata" -> config.misc.itemAgeData
            "autoupdates" -> config.autoUpdates
            "debug" -> config.debug
            else -> false
        }
    }
}
