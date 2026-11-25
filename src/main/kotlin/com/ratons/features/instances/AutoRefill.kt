package com.ratons.features.instances

import com.ratons.config.ConfigManager
import com.ratons.data.SkyBlockState
import com.ratons.utils.ChatUtils
import net.minecraft.client.Minecraft

/**
 * SkyHanni offered sack refills through an API; the server command it wrapped is public,
 * so this counts the inventory and sends it directly.
 */
object AutoRefill {

    private val mc get() = Minecraft.getInstance()
    private val config get() = ConfigManager.config.autoRefill

    fun onDungeonStart() = refill()
    fun onKuudraStart() = refill()

    private fun refill() {
        if (!config.enabled) return
        for (refillable in Refillables.entries) {
            val wanted = when (refillable) {
                Refillables.ENDER_PEARL -> config.pearls
                Refillables.DECOY -> config.decoys && SkyBlockState.floor?.endsWith("4") == true
                Refillables.INFLATABLE_JERRY -> config.jerrys && SkyBlockState.floor?.startsWith("M") == true
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
