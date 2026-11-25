package com.ratons.mixins

import com.ratons.data.ChatEvents
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(ClientPacketListener::class)
class MixinClientPacketListener {

    @Inject(method = ["handleSystemChat"], at = [At("HEAD")])
    private fun `ratons$onSystemChat`(packet: ClientboundSystemChatPacket, callback: CallbackInfo) {
        ChatEvents.onMessage(packet.content().string)
    }
}
