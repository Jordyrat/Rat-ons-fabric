package com.ratons.features.instances

import com.ratons.config.AutoRefillConfig
import com.ratons.data.SkyBlockState
import com.ratons.utils.ChatUtils
import net.minecraft.client.Minecraft
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor

/**
 * Counts what the instance needs against the inventory, then sends the server's refill command
 * for the shortfall. SkyblockAPI reports sack changes as deltas rather than totals, so the
 * sack balance is not checked before asking; the server ignores a request it cannot fill.
 */
object AutoRefill {

    private val mc get() = Minecraft.getInstance()
    private val config get() = AutoRefillConfig

    fun onDungeonStart() = refill()
    fun onKuudraStart() = refill()

    private fun refill() {
        if (!config.enabled) return
        for (refillable in Refillables.entries) {
            // Decoys are split across the boss room on F4 and its Master Mode twin, and
            // Inflatable Jerrys only matter on M7.
            val wanted = when (refillable) {
                Refillables.ENDER_PEARL -> config.pearls
                Refillables.DECOY -> config.decoys &&
                    (SkyBlockState.floor == DungeonFloor.F4 || SkyBlockState.floor == DungeonFloor.M4)
                Refillables.INFLATABLE_JERRY -> config.jerrys && SkyBlockState.floor == DungeonFloor.M7
            }
            if (!wanted) continue

            val missing = refillable.stackSize - countInInventory(refillable)
            if (missing <= 0) continue

            ChatUtils.debug("Refilling ${refillable.displayName} x$missing")
            ChatUtils.sendCommand("gfs ${refillable.sackName} $missing")
        }
    }

    private fun countInInventory(refillable: Refillables): Int {
        val inventory = mc.player?.inventory ?: return 0
        return inventory.nonEquipmentItems
            .filter { it.hoverName.string.contains(refillable.displayName, ignoreCase = true) }
            .sumOf { it.count }
    }
}

enum class Refillables(
    val sackName: String,
    val displayName: String,
    val stackSize: Int,
) {
    ENDER_PEARL("ENDER_PEARL", "Ender Pearl", 16),
    DECOY("DUNGEON_DECOY", "Decoy", 64),
    INFLATABLE_JERRY("INFLATABLE_JERRY", "Inflatable Jerry", 64),
}
