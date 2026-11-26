package com.ratons.config

import com.ratons.Ratons
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigElementRenderer
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigUI
import com.teamresourceful.resourcefulconfig.api.client.options.ResourcefulConfigOptionUI
import com.teamresourceful.resourcefulconfig.api.types.elements.ResourcefulConfigEntryElement
import com.teamresourceful.resourcefulconfig.api.types.entries.ResourcefulConfigValueEntry
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

/**
 * Resourceful Config draws an enum as a dropdown. Update Mode always holds exactly one of three
 * values, so it reads better as the row the select widget draws, with the current mode
 * highlighted instead of hidden behind a menu.
 */
object UpdateModeRenderer {

    val id: Identifier = Identifier.parse("${Ratons.MOD_ID}:update_mode")

    fun register() {
        ResourcefulConfigUI.registerElementRenderer(id) { element ->
            val entry = element as? ResourcefulConfigEntryElement
            Renderer(entry?.entry() as? ResourcefulConfigValueEntry, entry)
        }
    }

    private class Renderer(
        private val value: ResourcefulConfigValueEntry?,
        private val entry: ResourcefulConfigEntryElement?,
    ) : ResourcefulConfigElementRenderer {

        private val data get() = entry?.entry()?.options()

        override fun title(): Component = data?.title()?.toComponent() ?: Component.empty()

        override fun description(): Component = data?.comment()?.toComponent() ?: Component.empty()

        override fun widgets(): List<AbstractWidget> {
            val value = value ?: return emptyList()
            return listOf(
                ResourcefulConfigOptionUI.select(
                    title(),
                    UpdateMode.entries,
                    { listOfNotNull(value.getEnum() as? UpdateMode) },
                    { selected -> (selected.firstOrNull() as? UpdateMode)?.let { value.setEnum(it) } },
                ),
            )
        }
    }
}
