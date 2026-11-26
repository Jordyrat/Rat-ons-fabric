package com.ratons.commands

import com.ratons.Ratons
import com.ratons.config.AutoRefillConfig
import com.ratons.config.ConfigManager
import com.ratons.config.DevConfig
import com.ratons.config.RatConfig
import com.ratons.config.DungeonsConfig
import com.ratons.config.MiscConfig
import com.ratons.config.PartyFinderConfig
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
            read = { DungeonsConfig.relicSpawnTimer },
            write = { on -> DungeonsConfig.relicSpawnTimer = on },
        ),
        Toggle(
            "closechest",
            "Dungeons > Close Secret Chest",
            read = { DungeonsConfig.closeSecretChest },
            write = { on -> DungeonsConfig.closeSecretChest = on },
        ),
        Toggle(
            "autorefill",
            "Instances > Auto Refill",
            read = { AutoRefillConfig.enabled },
            write = { on -> AutoRefillConfig.enabled = on },
        ),
        Toggle(
            "refillpearls",
            "Instances > Auto Refill > Ender Pearls",
            read = { AutoRefillConfig.pearls },
            write = { on -> AutoRefillConfig.pearls = on },
        ),
        Toggle(
            "refilldecoys",
            "Instances > Auto Refill > Decoys",
            read = { AutoRefillConfig.decoys },
            write = { on -> AutoRefillConfig.decoys = on },
        ),
        Toggle(
            "refilljerrys",
            "Instances > Auto Refill > Inflatable Jerrys",
            read = { AutoRefillConfig.jerrys },
            write = { on -> AutoRefillConfig.jerrys = on },
        ),
        Toggle(
            "partyfinder",
            "Instances > Party Size Display",
            read = { PartyFinderConfig.partySizeDisplay },
            write = { on -> PartyFinderConfig.partySizeDisplay = on },
        ),
        Toggle(
            "dungeonitemdata",
            "Misc > Dungeon Item Data",
            read = { MiscConfig.dungeonItemData },
            write = { on -> MiscConfig.dungeonItemData = on },
        ),
        Toggle(
            "itemagedata",
            "Misc > Item Age Data",
            read = { MiscConfig.itemAgeData },
            write = { on -> MiscConfig.itemAgeData = on },
        ),
        Toggle("autoupdates", "About > Auto Updates", read = { RatConfig.autoUpdates }, write = { on -> RatConfig.autoUpdates = on }),
        Toggle("debug", "Dev > Debug", read = { DevConfig.debug }, write = { on -> DevConfig.debug = on }),
        Toggle(
            "debugchat",
            "Dev > Debug Chat Messages",
            read = { DevConfig.debugChat },
            write = { on -> DevConfig.debugChat = on },
        ),
    ).associateBy { it.name }

    private class Toggle(
        val name: String,
        val label: String,
        val read: () -> Boolean,
        val write: (Boolean) -> Unit,
    )

    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                literal("rat").executes {
                    ConfigManager.openScreen(null)
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
            val state = if (toggle.read()) "§aenabled" else "§7disabled"
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
        val now = !toggle.read()
        toggle.write(now)
        ConfigManager.save()
        ChatUtils.chat("§a${toggle.label} is now ${if (now) "enabled" else "disabled"}.")
    }
}
