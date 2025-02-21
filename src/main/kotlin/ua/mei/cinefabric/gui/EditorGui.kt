package ua.mei.cinefabric.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.HotbarGui
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.Brightness
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.BundleS2CPacket
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.joml.Vector3d
import org.joml.plus
import ua.mei.cinefabric.entity.VirtualDisplayEntity
import ua.mei.cinefabric.scene.Cutscene
import ua.mei.cinefabric.util.HeadModels
import kotlin.math.atan2
import kotlin.math.sqrt

class EditorGui(player: ServerPlayerEntity, val cutscene: Cutscene) : HotbarGui(player) {
    private val displays: MutableMap<Cutscene.Keyframe, KeyframeDisplay> = cutscene.keyframes.associateWithTo(mutableMapOf()) { KeyframeDisplay(it) }

    init {
        setSlot(
            0,
            GuiElementBuilder.from(Items.BARRIER.defaultStack)
                .setCallback { ->
                    close()
                }
        )

        setSlot(
            4,
            GuiElementBuilder.from(Items.EMERALD.defaultStack)
                .setCallback { ->
                    val keyframe: Cutscene.Keyframe = Cutscene.Keyframe(
                        player.x,
                        player.y,
                        player.z,
                        player.yaw,
                        player.pitch,
                        10,
                        Cutscene.Interpolation.LINEAR
                    )

                    cutscene.keyframes += keyframe
                    displays[keyframe] = KeyframeDisplay(keyframe).also { it.spawn() }
                    cutscene.keyframes.getOrNull(cutscene.keyframes.indexOf(keyframe) - 1)?.let { displays[it] }?.run {
                        updateTrack()
                        sync()
                    }
                }
        )

        setSlot(
            8,
            GuiElementBuilder.from(Items.EMERALD.defaultStack)
                .setCallback { ->
                    if (cutscene.keyframes.isNotEmpty()) {
                        close()

                        cutscene.play(player) {
                            displays.values.forEach { it.respawn() }
                            open()
                        }
                    }
                }
        )
    }

    override fun onClose() {
        displays.values.forEach { it.destroy() }
    }

    inner class KeyframeDisplay(val keyframe: Cutscene.Keyframe) {
        val camera: VirtualDisplayEntity = VirtualDisplayEntity(player, EntityType.ITEM_DISPLAY)
        val track: VirtualDisplayEntity = VirtualDisplayEntity(player, EntityType.ITEM_DISPLAY)
        val label: VirtualDisplayEntity = VirtualDisplayEntity(player, EntityType.TEXT_DISPLAY)

        init {
            camera.setBrightness(Brightness.FULL)
            track.setBrightness(Brightness.FULL)
            label.setBrightness(Brightness.FULL)

            label.setBillboardMode(DisplayEntity.BillboardMode.CENTER)

            camera.setTranslation(0f, 0.25f, 0f)
            track.setTranslation(0f, 0.03125f, 0f)

            camera.dataTracker.set(DisplayEntity.ItemDisplayEntity.ITEM, HeadModels.camera)
            track.dataTracker.set(DisplayEntity.ItemDisplayEntity.ITEM, HeadModels.track)
            label.dataTracker.set(DisplayEntity.TextDisplayEntity.TEXT, Text.literal("${keyframe.duration}t"))
        }

        fun spawn() {
            updateCamera()
            updateTrack()

            player.networkHandler.sendPacket(
                BundleS2CPacket(
                    buildList {
                        add(camera.createSpawnPacket())
                        add(track.createSpawnPacket())
                        add(label.createSpawnPacket())

                        camera.createSyncPackets(false).forEach(::add)
                        track.createSyncPackets(false).forEach(::add)
                        label.createSyncPackets(false).forEach(::add)
                    }
                )
            )
        }

        fun respawn() {
            player.networkHandler.sendPacket(
                BundleS2CPacket(
                    buildList {
                        add(camera.createSpawnPacket())
                        add(track.createSpawnPacket())
                        add(label.createSpawnPacket())

                        add(EntityTrackerUpdateS2CPacket(camera.entityId, camera.dataTracker.entries.values.map { it.toSerialized() }))
                        add(EntityTrackerUpdateS2CPacket(track.entityId, track.dataTracker.entries.values.map { it.toSerialized() }))
                        add(EntityTrackerUpdateS2CPacket(label.entityId, label.dataTracker.entries.values.map { it.toSerialized() }))
                    }
                )
            )
        }

        fun updateCamera() {
            camera.pos.set(keyframe.x, keyframe.y, keyframe.z)
            camera.rotation.apply {
                yaw = keyframe.yaw + 180f
                pitch = -keyframe.pitch
            }
        }

        fun updateTrack() {
            val next: Cutscene.Keyframe = cutscene.keyframes.getOrNull(cutscene.keyframes.indexOf(keyframe) + 1) ?: keyframe

            val vector: Vector3d = Vector3d(keyframe.x, keyframe.y, keyframe.z)
            val nextVector: Vector3d = Vector3d(next.x, next.y, next.z)

            val midVector: Vector3d = vector.plus(nextVector).mul(0.5)
            val diff: Vector3d = vector.sub(nextVector)

            val length: Float = diff.length().toFloat()

            val yaw: Float = Math.toDegrees(atan2(-diff.x, diff.z)).toFloat()
            val pitch: Float = Math.toDegrees(atan2(diff.y, sqrt(diff.x * diff.x + diff.z * diff.z))).toFloat()

            track.pos.set(midVector)
            track.rotation.apply {
                this.yaw = yaw + 180
                this.pitch = pitch
            }
            track.setScale(0.125f, 0.125f, length * 2)

            label.pos.set(midVector.x, midVector.y + 0.5f, midVector.z)
        }

        fun sync() {
            player.networkHandler.sendPacket(
                BundleS2CPacket(
                    buildList {
                        camera.createSyncPackets(true).forEach(::add)
                        track.createSyncPackets(true).forEach(::add)
                        label.createSyncPackets(true).forEach(::add)
                    }.takeIf { !it.isEmpty() } ?: return
                )
            )
        }

        fun destroy() {
            player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(camera.entityId, track.entityId, label.entityId))
        }
    }
}