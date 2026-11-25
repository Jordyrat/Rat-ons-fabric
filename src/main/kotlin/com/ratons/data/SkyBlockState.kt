package com.ratons.data

import net.minecraft.client.Minecraft

/**
 * Stands in for SkyHanni's island/dungeon tracking. Hypixel publishes all of this in the
 * tab list, which is what both mods read; this keeps only what Rat-ons needs.
 */
object SkyBlockState {

    private val mc get() = Minecraft.getInstance()

    private val areaPattern = Regex("Area: (.+)")
    private val floorPattern = Regex("The Catacombs \\((F\\d|M\\d)\\)")
    private val classPattern = Regex("\\((Berserk|Mage|Archer|Tank|Healer)\\)")

    var area: String? = null
        private set

    var floor: String? = null
        private set

    var playerClass: DungeonClass? = null
        private set

    var inBossRoom: Boolean = false
        private set

    val onSkyBlock: Boolean get() = area != null
    val inDungeon: Boolean get() = floor != null

    fun update() {
        val lines = tabListLines()
        if (lines.isEmpty()) return
        area = lines.firstNotNullOfOrNull { areaPattern.find(it)?.groupValues?.get(1) }
        floor = lines.firstNotNullOfOrNull { floorPattern.find(it)?.groupValues?.get(1) }
        playerClass = lines.firstNotNullOfOrNull { line ->
            classPattern.find(line)?.groupValues?.get(1)?.let { DungeonClass.byName(it) }
        }
        inBossRoom = inDungeon && lines.any { it.contains("Boss:", ignoreCase = true) }
    }

    fun reset() {
        area = null
        floor = null
        playerClass = null
        inBossRoom = false
    }

    private fun tabListLines(): List<String> =
        mc.connection?.listedOnlinePlayers?.mapNotNull { it.tabListDisplayName?.string } ?: emptyList()

    enum class DungeonClass {
        ARCHER, BERSERK, HEALER, MAGE, TANK;

        companion object {
            fun byName(name: String): DungeonClass? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }
}
