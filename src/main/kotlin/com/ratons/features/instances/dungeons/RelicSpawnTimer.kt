package com.ratons.features.instances.dungeons

import com.ratons.config.ConfigManager
import com.ratons.data.ChatEvents
import com.ratons.data.SkyBlockState
import com.ratons.utils.ChatUtils
import com.ratons.utils.TimeFormat.shortFormat
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Watches Necron's phase message, waits out the four seconds before relics spawn, and puts
 * the remaining time on the HUD. The 1.8.9 version drew the text at each relic's world
 * position; the HUD keeps the timing signal without depending on a specific render hook.
 */
object RelicSpawnTimer {

    private val config get() = ConfigManager.config.dungeons

    private const val NPC_PREFIX = "[BOSS] Necron"
    private const val START_MESSAGE = "ARGH!"

    private val elementId = Identifier.fromNamespaceAndPath("ratons", "relic_timer")

    private var messagesSent = 0
    private var remaining: Duration = Duration.ZERO

    fun register() {
        ChatEvents.register { message ->
            if (!enabled()) return@register
            if (!message.contains(NPC_PREFIX) || !message.contains(START_MESSAGE)) return@register
            messagesSent++
            if (messagesSent != 2) return@register
            ChatUtils.debug("Starting relic timer")
            remaining = 4.seconds
        }

        HudElementRegistry.addLast(elementId, HudElement { graphics, _ ->
            if (!enabled() || remaining <= Duration.ZERO) return@HudElement
            val client = Minecraft.getInstance()
            val text = "§dRelics: ${remaining.shortFormat()}"
            val x = (graphics.guiWidth() - client.font.width(text)) / 2
            graphics.text(client.font, text, x, 40, 0xFFFFFF, true)
        })
    }

    fun tick() {
        if (remaining > Duration.ZERO) {
            remaining = (remaining - 50.milliseconds).coerceAtLeast(Duration.ZERO)
        }
    }

    fun reset() {
        messagesSent = 0
        remaining = Duration.ZERO
    }

    private fun enabled() = config.relicSpawnTimer && SkyBlockState.inDungeon && SkyBlockState.floor == "M7"
}
