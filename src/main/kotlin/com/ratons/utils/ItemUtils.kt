package com.ratons.utils

import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack

/**
 * Hypixel keeps its item data in an ExtraAttributes compound. On modern versions the server
 * sends it inside the custom_data component, which is where these read from.
 */
object ItemUtils {

    private val RARITY_NAMES = listOf(
        "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "MYTHIC", "DIVINE", "SPECIAL", "VERY SPECIAL",
    )

    private fun ItemStack.extraAttributes(): CompoundTag {
        val tag = get(DataComponents.CUSTOM_DATA)?.copyTag() ?: return CompoundTag()
        return if (tag.contains("ExtraAttributes")) tag.getCompound("ExtraAttributes").orElse(CompoundTag()) else tag
    }

    fun ItemStack.getIntAttribute(key: String): Int? {
        val tag = extraAttributes()
        if (!tag.contains(key)) return null
        return tag.getInt(key).orElse(0).takeUnless { it == 0 }
    }

    fun ItemStack.getLongAttribute(key: String): Long? {
        val tag = extraAttributes()
        if (!tag.contains(key)) return null
        return tag.getLong(key).orElse(0L).takeUnless { it == 0L }
    }

    fun ItemStack.loreLines(): List<String> = get(DataComponents.LORE)?.lines()?.map { it.string } ?: emptyList()

    fun ItemStack.dungeonTier(): Int? = getIntAttribute("item_tier")

    fun ItemStack.dungeonStatBoost(): Int? = getIntAttribute("baseStatBoostPercentage")

    fun ItemStack.itemTimestamp(): Long? = getLongAttribute("timestamp")

    fun ItemStack.isDungeonItem(): Boolean = loreLines().any { it.contains("DUNGEON ") }

    /** Index of the rarity line, which is where the extra tooltip lines get inserted above. */
    fun ItemStack.rarityLoreIndex(lore: List<String>): Int {
        val rarity = RARITY_NAMES.firstOrNull { name -> lore.any { it.contains(name, ignoreCase = true) } } ?: return -1
        return lore.indexOfLast { it.contains(rarity, ignoreCase = true) }
    }
}
