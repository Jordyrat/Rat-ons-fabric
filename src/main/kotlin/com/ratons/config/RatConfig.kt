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
/** What the launch-time update check is allowed to do. */
enum class UpdateMode {
    None,
    Notify,
    Automatic,
}

object RatConfig : ConfigKt("ratons/config") {

    override val name: TranslatableValue = TranslatableValue.literal("Rat-ons")
    override val description: TranslatableValue =
        TranslatableValue.literal("Hypixel SkyBlock quality-of-life features")
    override val links: Array<ResourcefulConfigLink> = emptyArray()

    var updateMode by enum(UpdateMode.Automatic) {
        translation = "Update Mode"
        renderer = UpdateModeRenderer.id
        description = TranslatableValue.literal(
            """
            None: skips the automatic check when the game starts.
            Notify: tells you in chat when a newer release is published.
            Automatic: downloads it and replaces this jar when you close the game.
            """.trimIndent(),
        )
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
        description = TranslatableValue.literal("Displays a timer for when relics will spawn.")
    }

    var closeSecretChest by boolean(false) {
        translation = "Close Secret Chest"
        description = TranslatableValue.literal("Automatically closes secret chests when opened.")
    }
}

object AutoRefillConfig : CategoryKt("autoRefill") {

    override val name: TranslatableValue = TranslatableValue.literal("Auto Refill")

    var enabled by boolean(false) {
        translation = "Enabled"
        description = TranslatableValue.literal("Automatically refill items at the start of an instance (Required for below options).")
    }

    var pearls by boolean(false) {
        translation = "Refill Ender Pearls"
        description = TranslatableValue.literal("Enables auto refill for Ender Pearls.")
    }

    var decoys by boolean(false) {
        translation = "Refill Decoys"
        description = TranslatableValue.literal("Enables auto refill for Decoys on F4/M4.")
    }

    var jerrys by boolean(false) {
        translation = "Refill Inflatable Jerrys"
        description = TranslatableValue.literal("Enables auto refill for Inflatable Jerrys on M7.")
    }
}

object PartyFinderConfig : CategoryKt("partyFinder") {

    override val name: TranslatableValue = TranslatableValue.literal("Party Finder")

    var partySizeDisplay by boolean(false) {
        translation = "Party Size Display"
        description = TranslatableValue.literal("Displays party size as an item stack.")
    }
}

object MiscConfig : CategoryKt("misc") {

    override val name: TranslatableValue = TranslatableValue.literal("Misc")

    var dungeonItemData by boolean(false) {
        translation = "Dungeon Item Data"
        description = TranslatableValue.literal("Displays data about dungeon items drops in its lore.")
    }

    var itemAgeData by boolean(false) {
        translation = "Item Age Data"
        description = TranslatableValue.literal("Displays an item's age in its lore.")
    }
}

object DevConfig : CategoryKt("dev") {

    override val name: TranslatableValue = TranslatableValue.literal("Dev")

    var debug by boolean(false) {
        translation = "Debug"
        description = TranslatableValue.literal("Developer setting for checking debug messages.")
    }

    var debugChat by boolean(false) {
        translation = "Debug Chat Messages"
        description = TranslatableValue.literal("Also prints those debug messages into chat.")
    }
}
