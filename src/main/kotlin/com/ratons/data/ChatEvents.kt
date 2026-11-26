package com.ratons.data

import com.ratons.utils.ErrorManager
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI
import tech.thatgravyboat.skyblockapi.api.events.chat.ChatReceivedEvent

/**
 * Chat is the main input for dungeon and Kuudra state on SkyBlock. SkyblockAPI already hooks
 * the client's chat handling, so this subscribes to it instead of mixing into the packet
 * listener, and hands the text to whichever features care.
 */
object ChatEvents {

    private val listeners = mutableListOf<(String) -> Unit>()

    fun register(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    fun subscribe() {
        SkyBlockAPI.eventBus.register<ChatReceivedEvent> { event ->
            val message = event.text.trim()
            if (message.isEmpty()) return@register
            for (listener in listeners) {
                ErrorManager.guard("processing a chat message") { listener(message) }
            }
        }
    }
}
