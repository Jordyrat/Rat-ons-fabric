package com.ratons.features.instances.dungeons

import com.ratons.config.ConfigManager
import com.ratons.data.SkyBlockState
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen

/**
 * The 1.8.9 version cancelled the open-window packet. Closing the screen right after it opens
 * reaches the same end state, and the server closes the container on the next tick.
 */
object DungeonFeatures {

    private val config get() = ConfigManager.config.dungeons

    fun register() {
        ScreenEvents.AFTER_INIT.register { client, screen, _, _ ->
            if (screen !is AbstractContainerScreen<*>) return@register
            if (!config.closeSecretChest || !SkyBlockState.inDungeon) return@register
            if (screen.title.string != "Chest") return@register

            client.execute {
                if (Minecraft.getInstance().gui.screen() === screen) {
                    client.player?.closeContainer()
                }
            }
        }
    }
}
