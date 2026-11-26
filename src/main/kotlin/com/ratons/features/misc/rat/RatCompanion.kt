package com.ratons.features.misc.rat

import com.mojang.blaze3d.vertex.PoseStack
import com.ratons.Ratons
import com.ratons.config.MiscConfig

import com.ratons.utils.ErrorManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.entity.state.AvatarRenderState
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.model.player.PlayerModel
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sin

/**
 * A rat that rides the shoulder of the people who contributed to the mod.
 *
 * It is drawn entirely on the client, inside the player's own render pass, so it inherits the
 * player's position and rotation exactly and never needs the server: no entity is spawned, nothing
 * is sent over the network, and every player running Rat-ons sees the same rat on the same people.
 */
object RatCompanion {

    /**
     * Real rat varieties, each with its own sheet: fur, then ears and tail, eyes and belly.
     *
     * The breeds to pick from, when adding someone below:
     *
     * - [Agouti]    wild brown-grey, the colour of a common fancy rat
     * - [Albino]    white coat, pink eyes
     * - [Hooded]    white body with a coloured head and a saddle down the back
     * - [Black]     near-black coat, ruby eyes
     * - [Blue]      slate grey-blue
     * - [Himalayan] cream coat with dark points on the face, ears and tail
     */
    enum class RatBreed(file: String) {
        Agouti("rat_agouti"),
        Albino("rat_albino"),
        Hooded("rat_hooded"),
        Black("rat_black"),
        Blue("rat_blue"),
        Himalayan("rat_himalayan"),
        ;

        val texture: Identifier = Identifier.parse("ratons:textures/entity/$file.png")
    }

    /** Contributors, by UUID, and the breed of rat they carry. */
    private val CONTRIBUTORS: Map<UUID, RatBreed> = mapOf(
        UUID.fromString("45726bfa-4aee-402b-952c-93a1dece512e") to RatBreed.Hooded, // Jordyrat
        UUID.fromString("2cbd42c9-ef29-424b-8663-155d68c1df4b") to RatBreed.Agouti, // xJordyrat
    )

    internal const val FULL_BRIGHT = 0xF000F0

    private const val ALERT_MIN_TICKS = 180
    private const val ALERT_SPREAD_TICKS = 60
    private const val LYING_MIN_TICKS = 240
    private const val LYING_SPREAD_TICKS = 240

    private val states = ConcurrentHashMap<UUID, RatState>()
    internal val model: RatModel by lazy { RatModel() }
    private var announced = false

    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { tick() }
    }

    internal fun breedOf(uuid: UUID): RatBreed? = CONTRIBUTORS[uuid]

    internal fun snapshotFor(uuid: UUID): Snapshot {
        if (!announced) {
            announced = true
            Ratons.logger.info("Drawing the rat on $uuid")
        }
        return states.computeIfAbsent(uuid) { RatState() }.snapshot()
    }

    private fun tick() {
        val level = Minecraft.getInstance().level ?: return
        for (player in level.players()) {
            if (player.uuid !in CONTRIBUTORS) continue
            states.computeIfAbsent(player.uuid) { RatState() }.tick()
        }
    }

    private enum class Mode { Lying, Alert }

    /** The animated state of one rat, advanced a tick at a time and read once per frame. */
    internal class RatState {
        private var mode = Mode.Lying
        private var modeTicks = LYING_MIN_TICKS + (0..LYING_SPREAD_TICKS).random()
        private var alertness = 0f

        private var lookYaw = 0f
        private var lookPitch = 0f
        private var targetYaw = 0f
        private var targetPitch = 0f
        private var lookTicks = 0

        private var earTilt = 0f
        private var earTicks = 0
        private var tailPhase = 0f

        fun tick() {
            if (--modeTicks <= 0) nextMode()

            val targetSit = if (mode == Mode.Alert) 1f else 0f
            alertness += (targetSit - alertness) * 0.18f

            if (--lookTicks <= 0) pickLookTarget()

            lookYaw += (targetYaw - lookYaw) * 0.12f
            lookPitch += (targetPitch - lookPitch) * 0.12f

            if (--earTicks <= 0) {
                earTicks = 30 + (0..90).random()
                earTilt = if ((0..2).random() == 0) 0.45f else 0f
            }

            tailPhase += 0.12f
        }

        private fun nextMode() {
            mode = when (mode) {
                Mode.Lying -> if ((0..2).random() == 0) Mode.Alert else Mode.Lying
                Mode.Alert -> Mode.Lying
            }
            modeTicks = when (mode) {
                Mode.Alert -> ALERT_MIN_TICKS + (0..ALERT_SPREAD_TICKS).random()
                Mode.Lying -> LYING_MIN_TICKS + (0..LYING_SPREAD_TICKS).random()
            }
            if (mode == Mode.Alert) {
                targetYaw = 0f
                targetPitch = 0f
            }
        }

        private fun pickLookTarget() {
            lookTicks = 20 + (0..50).random()
            targetYaw = ((0..60).random() - 30) / 100f
            targetPitch = ((0..30).random() - 15) / 100f
        }

        fun snapshot() = Snapshot(alertness, lookYaw, lookPitch, earTilt, sin(tailPhase) * 0.25f)
    }

    internal class Snapshot(
        val alertness: Float,
        val lookYaw: Float,
        val lookPitch: Float,
        val earTilt: Float,
        val tailSway: Float,
    )

    /**
     * The rat's parts, built with +Y upwards and the head towards −Z. Sitting lifts the chest and
     * folds the hind legs under the body, so the rat perches rather than lying flat.
     */
    internal class RatModel {
        val root: ModelPart = LayerDefinition.create(shape(), 64, 64).bakeRoot()

        private val body = root.getChild("body")
        private val head = body.getChild("head")
        private val earLeft = head.getChild("ear_left")
        private val earRight = head.getChild("ear_right")
        private val tail = body.getChild("tail")
        private val frontLeft = body.getChild("front_left")
        private val frontRight = body.getChild("front_right")
        private val backLeft = body.getChild("back_left")
        private val backRight = body.getChild("back_right")

        fun pose(snapshot: Snapshot) {
            val alert = snapshot.alertness

            body.xRot = 0.5f * alert
            body.y = -0.3f * alert

            head.xRot = -0.35f * alert + snapshot.lookPitch
            head.yRot = snapshot.lookYaw

            earLeft.zRot = -snapshot.earTilt
            earRight.zRot = snapshot.earTilt

            tail.xRot = 0.35f * alert + 0.15f
            tail.yRot = snapshot.tailSway

            val fold = 1.25f * alert
            backLeft.xRot = fold
            backRight.xRot = fold
            frontLeft.xRot = -1.05f * alert
            frontRight.xRot = -1.05f * alert
            frontLeft.zRot = -0.25f * alert
            frontRight.zRot = 0.25f * alert
        }

        private companion object {
            fun shape(): MeshDefinition {
                val mesh = MeshDefinition()
                val root = mesh.root

                val body = root.addOrReplaceChild(
                    "body",
                    CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2f, -2f, -4f, 4f, 4f, 8f),
                    PartPose.offset(0f, 0f, 0f),
                )

                val head = body.addOrReplaceChild(
                    "head",
                    CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2f, -2f, -3f, 4f, 4f, 3f),
                    PartPose.offset(0f, 0.6f, -4f),
                )

                head.addOrReplaceChild(
                    "snout",
                    CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-1f, -0.75f, -1.5f, 2f, 1.5f, 1.5f),
                    PartPose.offset(0f, -0.9f, -3f),
                )

                head.addOrReplaceChild(
                    "ear_left",
                    CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-0.9f, 0f, -0.3f, 1.8f, 2f, 0.6f),
                    PartPose.offset(-1.4f, 1.9f, -1.2f),
                )

                head.addOrReplaceChild(
                    "ear_right",
                    CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-0.9f, 0f, -0.3f, 1.8f, 2f, 0.6f),
                    PartPose.offset(1.4f, 1.9f, -1.2f),
                )

                head.addOrReplaceChild(
                    "eye_left",
                    CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-0.3f, -0.3f, -0.2f, 0.6f, 0.6f, 0.4f),
                    PartPose.offset(-1.15f, 0.5f, -2.85f),
                )

                head.addOrReplaceChild(
                    "eye_right",
                    CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-0.3f, -0.3f, -0.2f, 0.6f, 0.6f, 0.4f),
                    PartPose.offset(1.15f, 0.5f, -2.85f),
                )

                body.addOrReplaceChild(
                    "tail",
                    CubeListBuilder.create()
                        .texOffs(32, 8)
                        .addBox(-0.3f, -0.3f, 0f, 0.6f, 0.6f, 7f),
                    PartPose.offsetAndRotation(0f, 0.4f, 4f, 0.15f, 0f, 0f),
                )

                body.addOrReplaceChild(
                    "belly",
                    CubeListBuilder.create()
                        .texOffs(32, 32)
                        .addBox(-1.8f, -0.2f, -3.5f, 3.6f, 0.4f, 7f),
                    PartPose.offset(0f, -1.9f, 0f),
                )

                for (name in listOf("front_left", "front_right")) {
                    val x = if (name == "front_left") -1.2f else 1.2f
                    body.addOrReplaceChild(
                        name,
                        CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-0.5f, -2.4f, -0.5f, 1f, 2.4f, 1f),
                        PartPose.offset(x, -2f, -2.6f),
                    )
                }

                for (name in listOf("back_left", "back_right")) {
                    val x = if (name == "back_left") -1.2f else 1.2f
                    body.addOrReplaceChild(
                        name,
                        CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-0.5f, -2.4f, -0.5f, 1f, 2.4f, 1f),
                        PartPose.offset(x, -2f, 2.6f),
                    )
                }

                return mesh
            }
        }
    }
}

/**
 * Draws the rat perched on its owner's head. The player's render pass has already placed, turned
 * and flipped the model, so this pivots at the neck by the head's own rotation, steps up to the
 * crown, and draws the rat upright in that space — a hat that looks around when its owner does.
 */
class RatShoulderLayer(parent: RenderLayerParent<AvatarRenderState, PlayerModel>) :
    RenderLayer<AvatarRenderState, PlayerModel>(parent) {

    override fun submit(
        poseStack: PoseStack,
        collector: SubmitNodeCollector,
        light: Int,
        state: AvatarRenderState,
        yRot: Float,
        xRot: Float,
    ) {
        val rider = (state as? RatRiderHolder)?.`ratons$getRider`() ?: return
        if (state.isInvisible) return
        val breed = RatCompanion.breedOf(rider) ?: return

        if (!MiscConfig.ratCompanion) return
        val snapshot = RatCompanion.snapshotFor(rider)
        ErrorManager.guard("drawing the rat") {
            val model = getParentModel()
            poseStack.pushPose()
            model.head.translateAndRotate(poseStack)
            poseStack.translate(0f, -HEAD_TOP, HEAD_BACK)
            poseStack.scale(1f, -1f, 1f)
            poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE)
            collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(breed.texture, false)) { pose, consumer ->
                val local = PoseStack()
                local.last().set(pose)
                RatCompanion.model.pose(snapshot)
                RatCompanion.model.root.render(local, consumer, RatCompanion.FULL_BRIGHT, OverlayTexture.NO_OVERLAY)
            }
            poseStack.popPose()
        }
    }

    private companion object {
        const val HEAD_TOP = 0.58f
        const val HEAD_BACK = 0.06f
        const val MODEL_SCALE = 0.8f
    }
}
