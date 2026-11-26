package com.ratons.features.instances

import com.ratons.config.PartyFinderConfig
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

/**
 * SkyHanni let a mod replace the stack count drawn on an item. Without that hook the party
 * size is drawn over each entry's icon, which is the same information in the same place.
 */
object PartyFinderFeatures {

    private val config get() = PartyFinderConfig

    private val partyNamePattern = Regex("'s Party$")
    private val dungeonPartyPattern = Regex("^Dungeon: ")

    private const val GUI_WIDTH = 176
    private const val GUI_HEIGHT = 166
    private const val EMPTY_SLOT = "Empty"

    fun register() {
        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (screen !is AbstractContainerScreen<*>) return@register
            if (!config.partySizeDisplay || screen.title.string != "Party Finder") return@register

            // The screen initializes before the server sends its slots, so the scan runs on
            // every frame instead of once here; listings also change while it stays open.
            ScreenEvents.afterForeground(screen).register { rendered, graphics, _, _, _ ->
                val container = rendered as? AbstractContainerScreen<*> ?: return@register
                val left = (container.width - GUI_WIDTH) / 2
                val top = (container.height - GUI_HEIGHT) / 2
                val font = Minecraft.getInstance().font
                for ((slotIndex, size) in scan(container)) {
                    val slot = (container as MenuAccess<*>).menu.slots.getOrNull(slotIndex) ?: continue
                    graphics.text(font, size.toString(), left + slot.x, top + slot.y + 9, 0xFFFFFF, true)
                }
            }
        }
    }

    private fun scan(screen: AbstractContainerScreen<*>): Map<Int, Int> {
        val sizes = mutableMapOf<Int, Int>()
        val menu = screen.menu
        for (slot in 0 until menu.slots.size) {
            val stack = menu.slots[slot].item
            if (stack.isEmpty) continue
            sizes[slot] = partySize(stack) ?: continue
        }
        return sizes
    }

    private fun partySize(stack: ItemStack): Int? {
        if (!partyNamePattern.containsMatchIn(stack.hoverName.string)) return null
        val lore = stack.get(DataComponents.LORE)?.lines()?.map { it.string } ?: return null
        val isDungeon = lore.firstOrNull()?.let { dungeonPartyPattern.containsMatchIn(it) } ?: false
        // Modern lore carries its color in Style rather than in the text, so compare plain.
        val openSlots = lore.count { plain(it).equals(EMPTY_SLOT, ignoreCase = true) }
        return (if (isDungeon) 5 else 4) - openSlots
    }

    private fun plain(line: String) = line.replace(Regex("§."), "").trim()
}
