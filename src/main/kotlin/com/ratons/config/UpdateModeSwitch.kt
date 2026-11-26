package com.ratons.config

import com.teamresourceful.resourcefulconfig.client.components.ModSprites
import com.teamresourceful.resourcefulconfig.client.components.options.types.BooleanOptionWidget
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import java.util.Locale

/**
 * A switch in the same dress as the ON/OFF rows: the panel's button sprite as the track, the
 * library's switch sprite under the active mode, and its own centred-string helper so the labels
 * sit and shade exactly like OFF and ON do. Resourceful Config's enum control and its select
 * widget both open a menu, which hides the other two states.
 */
class UpdateModeSwitch(
    private val getter: () -> UpdateMode,
    private val setter: (UpdateMode) -> Unit,
) : AbstractWidget(0, 0, SEGMENT_WIDTH * UpdateMode.entries.size, HEIGHT, Component.empty()) {

    private val labels = UpdateMode.entries.map { SHORT_LABELS.getValue(it) }

    override fun extractWidgetRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        val current = getter()
        val font = Minecraft.getInstance().font
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ModSprites.BUTTON, x, y, width, height)
        graphics.blitSprite(
            RenderPipelines.GUI_TEXTURED,
            ModSprites.ofSwitch(true),
            x + 1 + UpdateMode.entries.indexOf(current) * SEGMENT_WIDTH,
            y + 1,
            SEGMENT_WIDTH - 2,
            height - 2,
        )

        UpdateMode.entries.forEachIndexed { index, mode ->
            val left = x + index * SEGMENT_WIDTH
            BooleanOptionWidget.extractCenteredString(
                graphics,
                font,
                Component.literal(labels[index]),
                left + SEGMENT_WIDTH / 2,
                y + 4,
                if (mode == current) ACTIVE_TEXT else IDLE_TEXT,
                false,
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

        /** The colours the ON/OFF switch draws its highlighted and idle labels in. */
        private const val ACTIVE_TEXT = 0xFF727478.toInt()
        private const val IDLE_TEXT = 0xFFFAF9F6.toInt()

        // The same 40px halves the ON/OFF switch uses: three of them fit the widget column at
        // the library's smallest window instead of running over the description text.
        private const val SEGMENT_WIDTH = 40

        private val SHORT_LABELS = mapOf(
            UpdateMode.None to "NONE",
            UpdateMode.Notify to "NOTIFY",
            UpdateMode.Automatic to "AUTO",
        )
    }
}
