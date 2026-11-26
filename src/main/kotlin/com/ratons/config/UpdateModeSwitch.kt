package com.ratons.config

import net.minecraft.client.Minecraft
import com.teamresourceful.resourcefulconfig.client.components.ModSprites
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.TextAlignment
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.Locale

/**
 * A switch in the same dress as the ON/OFF rows: the panel's button sprite as the track and the
 * library's switch sprite sitting under the active mode. Resourceful Config's enum control and its
 * select widget both open a menu, which hides the other two states.
 */
class UpdateModeSwitch(
    private val getter: () -> UpdateMode,
    private val setter: (UpdateMode) -> Unit,
) : AbstractWidget(0, 0, SEGMENT_WIDTH * UpdateMode.entries.size, HEIGHT, Component.empty()) {

    private val labels = UpdateMode.entries.map { it.name.uppercase(Locale.ROOT) }

    override fun extractWidgetRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        val current = getter()
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ModSprites.BUTTON, x, y, width, height)
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            ModSprites.ofSwitch(true),
            x + 1 + UpdateMode.entries.indexOf(current) * SEGMENT_WIDTH,
            y + 1,
            SEGMENT_WIDTH - 2,
            height - 2,
        )

        val text = graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
        UpdateMode.entries.forEachIndexed { index, mode ->
            val left = x + index * SEGMENT_WIDTH
            text.accept(
                TextAlignment.CENTER,
                left + SEGMENT_WIDTH / 2,
                y + (height - 8) / 2,
                Component.literal(labels[index]).withColor(if (mode == current) ACTIVE_TEXT else IDLE_TEXT),
            )
        }
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        val index = ((event.x() - x) / SEGMENT_WIDTH).toInt().coerceIn(0, UpdateMode.entries.size - 1)
        setter(UpdateMode.entries[index])
    }

    override fun createNarrationMessage(): MutableComponent = wrapDefaultNarrationMessage(Component.literal(getter().name))

    override fun updateWidgetNarration(output: NarrationElementOutput) = defaultButtonNarrationText(output)

    companion object {
        private const val HEIGHT = 16

        // One width for all three, sized to the longest label so the longest one cannot spill
        // over its neighbour.
        private val SEGMENT_WIDTH: Int
            get() {
                val font = Minecraft.getInstance().font
                return UpdateMode.entries.maxOf { font.width(it.name.uppercase(Locale.ROOT)) } + 16
            }

        private const val ACTIVE_TEXT = 0xFF1A1A1A.toInt()
        private const val IDLE_TEXT = 0xFFA8A8A8.toInt()
    }
}
