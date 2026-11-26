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
    }

    var debug by boolean(false) {
        translation = "Debug"
    }

    init {
        category(DungeonsConfig)
        category(AutoRefillConfig)
        category(PartyFinderConfig)
        category(MiscConfig)
    }
}

object DungeonsConfig : CategoryKt("dungeons") {

    override val name: TranslatableValue = TranslatableValue.literal("Dungeons")

    var relicSpawnTimer by boolean(false) {
        translation = "Relic Spawn Timer"
    }

    var closeSecretChest by boolean(false) {
        translation = "Close Secret Chest"
    }
}

object AutoRefillConfig : CategoryKt("autoRefill") {

    override val name: TranslatableValue = TranslatableValue.literal("Auto Refill")

    var enabled by boolean(false) {
        translation = "Enabled"
    }

    var pearls by boolean(false) {
        translation = "Refill Ender Pearls"
    }

    var decoys by boolean(false) {
        translation = "Refill Decoys"
    }

    var jerrys by boolean(false) {
        translation = "Refill Inflatable Jerrys"
    }
}

object PartyFinderConfig : CategoryKt("partyFinder") {

    override val name: TranslatableValue = TranslatableValue.literal("Party Finder")

    var partySizeDisplay by boolean(false) {
        translation = "Party Size Display"
    }
}

object MiscConfig : CategoryKt("misc") {

    override val name: TranslatableValue = TranslatableValue.literal("Misc")

    var dungeonItemData by boolean(false) {
        translation = "Dungeon Item Data"
    }

    var itemAgeData by boolean(false) {
        translation = "Item Age Data"
    }

    var debugChat by boolean(false) {
        translation = "Debug Chat Messages"
    }
}
