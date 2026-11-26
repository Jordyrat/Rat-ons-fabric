package com.ratons.features.misc

import com.ratons.config.MiscConfig
import com.ratons.data.SkyBlockState
import com.ratons.utils.ItemUtils.dungeonStatBoost
import com.ratons.utils.ItemUtils.dungeonTier
import com.ratons.utils.ItemUtils.isDungeonItem
import com.ratons.utils.ItemUtils.itemTimestamp
import com.ratons.utils.ItemUtils.rarityLoreIndex
import com.ratons.utils.ErrorManager
import com.ratons.utils.SimpleTimeMark
import com.ratons.utils.TimeFormat.shortFormat
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

/**
 * Adds dungeon quality and item age lines under the rarity line in an item's tooltip.
 * SkyHanni used to give us the tooltip event; Fabric API's callback replaces it directly.
 */
object ItemDataDisplay {

    private val config get() = MiscConfig

    fun register() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            if (!config.dungeonItemData && !config.itemAgeData) return@register
            if (!SkyBlockState.onSkyBlock) return@register

            ErrorManager.guard("adding item data to a tooltip") {
                val rarityIndex = stack.rarityLoreIndex(lines.map { it.string })
                if (rarityIndex >= 0) {
                    var index = rarityIndex + 1
                    if (addDungeonQuality(index, stack, lines)) index++
                    addItemAge(index, stack, lines)
                }
            }
        }
    }

    private fun addDungeonQuality(index: Int, item: ItemStack, lore: MutableList<Component>): Boolean {
        if (!config.dungeonItemData) return false
        if (!item.isDungeonItem()) return false
        val tier = item.dungeonTier() ?: return false
        val quality = item.dungeonStatBoost() ?: return false
        val qualityColor = if (quality == 50) "§d" else "§6"
        val tierColor = if (tier == 10) "§d" else "§6"
        lore.add(index, Component.literal("§7Item Quality: +$qualityColor$quality% §7(Tier $tierColor$tier§7)"))
        return true
    }

    private fun addItemAge(index: Int, item: ItemStack, lore: MutableList<Component>): Boolean {
        if (!config.itemAgeData) return false
        val timestamp = item.itemTimestamp() ?: return false
        val age = SimpleTimeMark.epochMillis(timestamp).passedSince()
        lore.add(index, Component.literal("§7Item Age: §8${age.shortFormat()}"))
        return true
    }
}
