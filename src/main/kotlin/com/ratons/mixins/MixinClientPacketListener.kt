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

    // RETURN, not HEAD: handleSystemChat starts with ensureRunningOnSameThread, which hops to
    // the client thread and re-runs the handler. A HEAD hook would fire on the network thread,
    // and twice per message whenever the hop happens.
    @Inject(method = ["handleSystemChat"], at = [At("RETURN")])
    private fun `ratons$onSystemChat`(packet: ClientboundSystemChatPacket, callback: CallbackInfo) {
        if (packet.overlay()) return
        ChatEvents.onMessage(packet.content().string)
    }
}
