package com.ratons.config

import com.teamresourceful.resourcefulconfig.api.types.info.ResourcefulConfigLink
import com.teamresourceful.resourcefulconfig.api.types.options.TranslatableValue
import com.teamresourceful.resourcefulconfigkt.api.CategoryKt
import com.teamresourceful.resourcefulconfigkt.api.ConfigKt

/**
 * The mod's settings, declared with Resourceful Config's Kotlin DSL. Each `by boolean(...)`
 * property is persisted by the library and drawn as a widget in the config screen, which is
 * what `/rat` opens.
 */
object RatConfig : ConfigKt("ratons/config") {

    override val name: TranslatableValue = TranslatableValue.literal("Rat-ons")
    override val description: TranslatableValue =
        TranslatableValue.literal("Hypixel SkyBlock quality-of-life features")
    override val links: Array<ResourcefulConfigLink> = emptyArray()

    var autoUpdates by boolean(true) {
        translation = "Auto Updates"
        description = TranslatableValue.literal("Check GitHub for a newer release at launch, and say so in chat when there is one.")
    }

    init {
        category(DungeonsConfig)
        category(AutoRefillConfig)
        category(PartyFinderConfig)
        category(MiscConfig)
        category(DevConfig)
    }
}

object DungeonsConfig : CategoryKt("dungeons") {

    override val name: TranslatableValue = TranslatableValue.literal("Dungeons")

    var relicSpawnTimer by boolean(false) {
        translation = "Relic Spawn Timer"
        description = TranslatableValue.literal("Show a Dragon Relic timer while an M7 run is going.")
    }

    var closeSecretChest by boolean(false) {
        translation = "Close Secret Chest"
        description = TranslatableValue.literal("Close the chest screen as soon as a secret chest is opened.")
    }
}

object AutoRefillConfig : CategoryKt("autoRefill") {

    override val name: TranslatableValue = TranslatableValue.literal("Auto Refill")

    var enabled by boolean(false) {
        translation = "Enabled"
        description = TranslatableValue.literal("Top the stack back up before a dungeon or Kuudra run starts.")
    }

    var pearls by boolean(false) {
        translation = "Refill Ender Pearls"
        description = TranslatableValue.literal("Count Ender Pearls when refilling.")
    }

    var decoys by boolean(false) {
        translation = "Refill Decoys"
        description = TranslatableValue.literal("Count Decoys when refilling.")
    }

    var jerrys by boolean(false) {
        translation = "Refill Inflatable Jerrys"
        description = TranslatableValue.literal("Count Inflatable Jerrys when refilling.")
    }
}

object PartyFinderConfig : CategoryKt("partyFinder") {

    override val name: TranslatableValue = TranslatableValue.literal("Party Finder")

    var partySizeDisplay by boolean(false) {
        translation = "Party Size Display"
        description = TranslatableValue.literal("Draw the party size over the icon in the party finder list.")
    }
}

object MiscConfig : CategoryKt("misc") {

    override val name: TranslatableValue = TranslatableValue.literal("Misc")

    var dungeonItemData by boolean(false) {
        translation = "Dungeon Item Data"
        description = TranslatableValue.literal("Add the dungeon item data lines to item tooltips.")
    }

    var itemAgeData by boolean(false) {
        translation = "Item Age Data"
        description = TranslatableValue.literal("Add the item age line to item tooltips.")
    }
}

object DevConfig : CategoryKt("dev") {

    override val name: TranslatableValue = TranslatableValue.literal("Dev")

    var debug by boolean(false) {
        translation = "Debug"
        description = TranslatableValue.literal("Log what the mod is doing to the game log.")
    }

    var debugChat by boolean(false) {
        translation = "Debug Chat Messages"
        description = TranslatableValue.literal("Print that debug output into chat as well, not just the log.")
    }
}
