package com.ratons.data

import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonAPI
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonClass
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonFloor
import tech.thatgravyboat.skyblockapi.api.location.LocationAPI

/**
 * The SkyBlock facts the features need. SkyblockAPI tracks all of it from the live game, so
 * this is a thin reading of its state rather than the tab-list and sidebar parsing it replaces.
 */
object SkyBlockState {

    val onSkyBlock: Boolean get() = LocationAPI.isOnSkyBlock

    val area: String? get() = LocationAPI.area?.name

    val floor: DungeonFloor? get() = DungeonAPI.dungeonFloor

    val inDungeon: Boolean get() = floor != null

    val playerClass: DungeonClass? get() = DungeonAPI.dungeonClass

    val inBossRoom: Boolean get() = DungeonAPI.inBoss
}
