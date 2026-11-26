package com.ratons.features.misc.rat

import com.ratons.utils.ChatUtils
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * Development camera for looking at the rat from angles the player can never stand in. It orbits
 * the local player at [DISTANCE] blocks and takes the mouse, so the player stays exactly where it
 * was — frozen, still looking where it was looking — while the camera swings around it.
 *
 * Offline only: a real freecam would let the player walk while the camera stayed behind, but all
 * this needs to do is show the rat, so freezing the player is the whole trick. Remove this and the
 * Camera, KeyboardInput and EntityTurn mixins once the rat's placement is settled.
 */
object RatFreecam {

    private const val DISTANCE = 2.6
    private const val TURN_SCALE = 0.15

    var active = false
        private set

    private var yaw = 0f
    private var pitch = 0f

    fun toggle(minecraft: Minecraft) {
        if (!active && !minecraft.hasSingleplayerServer()) {
            ChatUtils.chat("§cThe freecam only works in a singleplayer world.")
            return
        }

        active = !active
        minecraft.options.cameraType = if (active) CameraType.THIRD_PERSON_BACK else CameraType.FIRST_PERSON
        if (active) {
            minecraft.player?.let { player ->
                yaw = player.yRot
                pitch = player.xRot
                player.isSprinting = false
            }
        }

        ChatUtils.chat(if (active) "§aFreecam on §7- the mouse orbits the player." else "§7Freecam off.")
    }

    /** The mouse, redirected from the player into the camera. */
    fun turn(yRot: Double, xRot: Double) {
        yaw += (yRot * TURN_SCALE).toFloat()
        pitch = (pitch + (xRot * TURN_SCALE).toFloat()).coerceIn(-90f, 90f)
    }

    /** Where the camera sits this frame, or null when the player has no world to stand in. */
    fun position(): Vec3? {
        val player = Minecraft.getInstance().player ?: return null
        val yawRad = Math.toRadians(yaw.toDouble())
        val pitchRad = Math.toRadians(pitch.toDouble())
        val look = Vec3(
            -sin(yawRad) * cos(pitchRad),
            -sin(pitchRad),
            cos(yawRad) * cos(pitchRad),
        )
        return player.getEyePosition().subtract(look.scale(DISTANCE))
    }

    fun yaw(): Float = yaw

    fun pitch(): Float = pitch
}
