package com.ratons.features.instances

import com.ratons.config.ConfigManager
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack

/**
 * SkyHanni let a mod replace the stack count drawn on an item. Without that hook the party
 * size is drawn over each entry's icon, which is the same information in the same place.
 */
object PartyFinderFeatures {

    private val config get() = ConfigManager.config.partyFinder

    private val partyNamePattern = Regex("'s Party$")
    private val dungeonPartyPattern = Regex("^Dungeon: ")

    private const val GUI_WIDTH = 176
    private const val GUI_HEIGHT = 166

    fun register() {
        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (screen !is AbstractContainerScreen<*>) return@register
            if (!config.partySizeDisplay || screen.title.string != "Party Finder") return@register

            val sizes = scan(screen)
            if (sizes.isEmpty()) return@register

            ScreenEvents.afterForeground(screen).register { rendered, graphics, _, _, _ ->
                val left = (rendered.width - GUI_WIDTH) / 2
                val top = (rendered.height - GUI_HEIGHT) / 2
                for ((slotIndex, size) in sizes) {
                    val slot = (rendered as MenuAccess<*>).menu.slots.getOrNull(slotIndex) ?: continue
                    graphics.text(
                        net.minecraft.client.Minecraft.getInstance().font,
                        size.toString(),
                        left + slot.x,
                        top + slot.y + 9,
                        0xFFFFFF,
                        true,
                    )
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
        val openSlots = lore.count { it == "§8 Empty" }
        return (if (isDungeon) 5 else 4) - openSlots
    }
}
