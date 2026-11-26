package com.ratons.utils

import com.ratons.Ratons
import com.ratons.config.DevConfig
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style

object ChatUtils {

    private const val PREFIX = "§e[Rat-ons] "
    private const val USER_ERROR_PREFIX = "§c[Rat-ons] "
    private const val DEBUG_PREFIX = "§7[Rat-ons Debug] §7"

    private val player get() = Minecraft.getInstance().player

    fun chat(message: String, prefix: Boolean = true) {
        send((if (prefix) PREFIX else "") + message)
    }

    fun userError(message: String) = send(USER_ERROR_PREFIX + message)

    fun debug(message: String) {
        if (!DevConfig.debug) return
        Ratons.logger.info(message)
        if (DevConfig.debugChat) send(DEBUG_PREFIX + message)
    }

    fun clickable(message: String, command: String, hover: String = "§eClick here!") {
        display(
            Component.literal(PREFIX + message).withStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.RunCommand(command))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal(hover))),
            ),
        )
    }

    fun openLink(message: String, url: String) {
        display(
            Component.literal(PREFIX + message).withStyle(
                Style.EMPTY
                    .withClickEvent(ClickEvent.OpenUrl(java.net.URI.create(url)))
                    .withHoverEvent(HoverEvent.ShowText(Component.literal("§eOpen $url"))),
            ),
        )
    }

    /** Runs a server command as if the player typed it. */
    fun sendCommand(command: String) {
        player?.connection?.sendCommand(command)
    }

    private fun send(message: String) = display(Component.literal(message))

    private fun display(component: Component) {
        Minecraft.getInstance().gui.hud.chat.addClientSystemMessage(component)
    }
}
