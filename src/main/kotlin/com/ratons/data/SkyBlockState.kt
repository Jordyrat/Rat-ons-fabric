package com.ratons.data

import net.minecraft.client.Minecraft
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerTeam

/**
 * Stands in for SkyHanni's island/dungeon tracking. Hypixel publishes the area and class in
 * the tab list and the current floor in the sidebar, so both are read and merged.
 */
object SkyBlockState {

    private val mc get() = Minecraft.getInstance()

    // REGEX-TEST: " §7Area: §bThe Catacombs"
    private val areaPattern = Regex("Area: (.+)")

    // REGEX-TEST: " §7The Catacombs §8(M7)"
    // REGEX-TEST: " The Catacombs (F7)"
    private val floorPattern = Regex("The Catacombs ?\\((F\\d|M\\d)\\)")

    // REGEX-TEST: "§b(Berserk)"
    private val classPattern = Regex("\\((Berserk|Mage|Archer|Tank|Healer)\\)")

    private val colorCodes = Regex("§.")

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
        val lines = sidebarLines() + tabListLines()
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

    /**
     * Sidebar text is not in the score holder's name: vanilla renders it by passing the name
     * through its team, which is where Hypixel puts the prefix and suffix that carry the real
     * text. Formatting through the team is what makes the floor visible.
     */
    private fun sidebarLines(): List<String> {
        val scoreboard = mc.level?.scoreboard ?: return emptyList()
        val objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) ?: return emptyList()
        val title = plain(objective.displayName.string)
        val entries = scoreboard.listPlayerScores(objective).map { entry ->
            val team = scoreboard.getPlayerTeam(entry.owner())
            val text = PlayerTeam.formatNameForTeam(team, entry.ownerName())
            plain(text.string)
        }
        return listOf(title) + entries
    }

    private fun plain(line: String) = line.replace(colorCodes, "")

    enum class DungeonClass {
        ARCHER, BERSERK, HEALER, MAGE, TANK;

        companion object {
            fun byName(name: String): DungeonClass? = entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
        }
    }
}
