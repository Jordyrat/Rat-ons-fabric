package com.ratons

import com.ratons.commands.RatCommands
import com.ratons.config.ConfigManager
import com.ratons.config.RatConfig
import com.ratons.data.ChatEvents
import com.ratons.data.SkyBlockState
import com.ratons.features.instances.AutoRefill
import com.ratons.features.instances.PartyFinderFeatures
import com.ratons.features.instances.dungeons.DungeonFeatures
import com.ratons.features.instances.dungeons.RelicSpawnTimer
import com.ratons.features.misc.ItemDataDisplay
import com.ratons.features.misc.update.UpdateManager
import com.ratons.utils.ChatUtils
import com.ratons.utils.ErrorManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Ratons : ClientModInitializer {

    private var ticks = 0

    override fun onInitializeClient() {
        ConfigManager.load()

        RatCommands.register()
        ItemDataDisplay.register()
        PartyFinderFeatures.register()
        DungeonFeatures.register()
        RelicSpawnTimer.register()

        ChatEvents.register(::onChat)

        ClientTickEvents.END_CLIENT_TICK.register {
            ticks++
            ErrorManager.guard("updating the relic timer") { RelicSpawnTimer.tick() }
            ErrorManager.guard("checking for updates") { UpdateManager.onTick() }
            if (ticks % TICKS_PER_STATE_UPDATE == 0) {
                ErrorManager.guard("reading the SkyBlock state") { SkyBlockState.update() }
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            SkyBlockState.reset()
            RelicSpawnTimer.reset()
        }

        logger.info("$MOD_NAME $version loaded")
    }

    private fun onChat(message: String) {
        if (message.contains("Starting a new dungeon")) AutoRefill.onDungeonStart()
        if (message.contains("I will go and fish up Kuudra")) AutoRefill.onKuudraStart()
        if (RatConfig.debug) ChatUtils.debug("chat: $message")
    }

    companion object {
        private const val TICKS_PER_STATE_UPDATE = 20

        const val MOD_ID = "ratons"
        const val MOD_NAME = "Rat-ons"

        @JvmField
        val logger: Logger = LoggerFactory.getLogger(MOD_NAME)

        val version: String by lazy {
            FabricLoader.getInstance().getModContainer(MOD_ID)
                .map { it.metadata.version.friendlyString }
                .orElse("unknown")
        }
    }
}
