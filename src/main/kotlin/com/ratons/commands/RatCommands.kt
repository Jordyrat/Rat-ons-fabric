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

    private val toggles: Map<String, Toggle> = listOf(
        Toggle(
            "relictimer",
            "Dungeons > Relic Spawn Timer",
            read = { it.dungeons.relicSpawnTimer },
            write = { c, on -> c.copy(dungeons = c.dungeons.copy(relicSpawnTimer = on)) },
        ),
        Toggle(
            "closechest",
            "Dungeons > Close Secret Chest",
            read = { it.dungeons.closeSecretChest },
            write = { c, on -> c.copy(dungeons = c.dungeons.copy(closeSecretChest = on)) },
        ),
        Toggle(
            "autorefill",
            "Instances > Auto Refill",
            read = { it.autoRefill.enabled },
            write = { c, on -> c.copy(autoRefill = c.autoRefill.copy(enabled = on)) },
        ),
        Toggle(
            "refillpearls",
            "Instances > Auto Refill > Ender Pearls",
            read = { it.autoRefill.pearls },
            write = { c, on -> c.copy(autoRefill = c.autoRefill.copy(pearls = on)) },
        ),
        Toggle(
            "refilldecoys",
            "Instances > Auto Refill > Decoys",
            read = { it.autoRefill.decoys },
            write = { c, on -> c.copy(autoRefill = c.autoRefill.copy(decoys = on)) },
        ),
        Toggle(
            "refilljerrys",
            "Instances > Auto Refill > Inflatable Jerrys",
            read = { it.autoRefill.jerrys },
            write = { c, on -> c.copy(autoRefill = c.autoRefill.copy(jerrys = on)) },
        ),
        Toggle(
            "partyfinder",
            "Instances > Party Size Display",
            read = { it.partyFinder.partySizeDisplay },
            write = { c, on -> c.copy(partyFinder = c.partyFinder.copy(partySizeDisplay = on)) },
        ),
        Toggle(
            "dungeonitemdata",
            "Misc > Dungeon Item Data",
            read = { it.misc.dungeonItemData },
            write = { c, on -> c.copy(misc = c.misc.copy(dungeonItemData = on)) },
        ),
        Toggle(
            "itemagedata",
            "Misc > Item Age Data",
            read = { it.misc.itemAgeData },
            write = { c, on -> c.copy(misc = c.misc.copy(itemAgeData = on)) },
        ),
        Toggle("autoupdates", "About > Auto Updates", read = { it.autoUpdates }, write = { c, on -> c.copy(autoUpdates = on) }),
        Toggle("debug", "About > Debug", read = { it.debug }, write = { c, on -> c.copy(debug = on) }),
    ).associateBy { it.name }

    private class Toggle(
        val name: String,
        val label: String,
        val read: (com.ratons.config.RatConfig) -> Boolean,
        val write: (com.ratons.config.RatConfig, Boolean) -> com.ratons.config.RatConfig,
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
        toggles.values.forEach { toggle ->
            val state = if (toggle.read(ConfigManager.config)) "§aenabled" else "§7disabled"
            ChatUtils.clickable("§7 - §f${toggle.name} §8(${toggle.label}) §8- $state", "/rat toggle ${toggle.name}")
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
        val toggle = toggles[key] ?: run {
            ChatUtils.userError("Unknown feature '$feature'. Try /rat for the list.")
            return
        }
        val now = !toggle.read(ConfigManager.config)
        ConfigManager.update { toggle.write(it, now) }
        ChatUtils.chat("§a${toggle.label} is now ${if (now) "enabled" else "disabled"}.")
    }
}
