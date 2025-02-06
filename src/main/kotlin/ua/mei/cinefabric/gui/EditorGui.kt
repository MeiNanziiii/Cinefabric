package ua.mei.cinefabric.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.HotbarGui
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.BundleS2CPacket
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import org.joml.Vector3d
import org.joml.Vector3f
import ua.mei.cinefabric.cast.CinefabricPlayer
import ua.mei.cinefabric.scene.Cutscene
import ua.mei.cinefabric.util.copy
import java.util.*
import kotlin.math.atan2
import kotlin.math.sqrt

class EditorGui(player: ServerPlayerEntity, val cutscene: Cutscene) : HotbarGui(player) {
    private val activeIds: MutableList<Int> = mutableListOf()

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
                    cutscene.keyframes += Cutscene.Keyframe(
                        player.x,
                        player.y,
                        player.z,
                        player.yaw,
                        player.pitch,
                        10
                    )

                    render()
                }
        )

        setSlot(
            8,
            GuiElementBuilder.from(Items.EMERALD.defaultStack)
                .setCallback { ->
                    if (cutscene.keyframes.isNotEmpty()) {
                        close()
                        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(IntArrayList.wrap(activeIds.toIntArray())))
                        activeIds.clear()

                        (player as CinefabricPlayer).playScene(cutscene) {
                            render()
                            open()
                        }
                    }
                }
        )
    }

    fun render() {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(IntArrayList.wrap(activeIds.toIntArray())))
        activeIds.clear()

        var entityId: Int = -1

        cutscene.keyframes.forEach {
            val id: Int = --entityId

            player.networkHandler.sendPacket(
                BundleS2CPacket(
                    listOf(
                        EntitySpawnS2CPacket(
                            id,
                            UUID.randomUUID(),
                            it.x,
                            it.y,
                            it.z,
                            it.pitch,
                            it.yaw,
                            EntityType.BLOCK_DISPLAY,
                            0,
                            Vec3d.ZERO,
                            0.0
                        ),
                        EntityTrackerUpdateS2CPacket(
                            id,
                            listOf(
                                DataTracker.SerializedEntry<BlockState>(
                                    DisplayEntity.BlockDisplayEntity.BLOCK_STATE.id,
                                    DisplayEntity.BlockDisplayEntity.BLOCK_STATE.dataType,
                                    Blocks.OBSERVER.defaultState
                                ),
                                DataTracker.SerializedEntry<Vector3f>(
                                    DisplayEntity.TRANSLATION.id,
                                    DisplayEntity.TRANSLATION.dataType,
                                    Vector3f(-0.5f)
                                )
                            )
                        )
                    )
                )
            )

            activeIds += id
        }
        if (cutscene.keyframes.size > 1) {
            (1..<cutscene.keyframes.size).forEach {
                val id: Int = --entityId

                val previousKeyframe: Cutscene.Keyframe = cutscene.keyframes[it - 1]
                val currentKeyframe: Cutscene.Keyframe = cutscene.keyframes[it]

                val previousVector: Vector3d = Vector3d(previousKeyframe.x, previousKeyframe.y, previousKeyframe.z)
                val currentVector: Vector3d = Vector3d(currentKeyframe.x, currentKeyframe.y, currentKeyframe.z)

                val mid: Vector3d = previousVector.copy().add(currentVector).mul(0.5)
                val diff: Vector3d = currentVector.copy().sub(previousVector)

                val length: Float = diff.length().toFloat()

                val yaw: Float = Math.toDegrees(atan2(-diff.x, diff.z)).toFloat() + 180f
                val pitch: Float = Math.toDegrees(atan2(diff.y, sqrt(diff.x * diff.x + diff.z * diff.z))).toFloat()

                player.networkHandler.sendPacket(
                    BundleS2CPacket(
                        listOf(
                            EntitySpawnS2CPacket(
                                id,
                                UUID.randomUUID(),
                                mid.x,
                                mid.y,
                                mid.z,
                                pitch,
                                yaw,
                                EntityType.BLOCK_DISPLAY,
                                0,
                                Vec3d.ZERO,
                                0.0
                            ),
                            EntityTrackerUpdateS2CPacket(
                                id,
                                listOf(
                                    DataTracker.SerializedEntry<BlockState>(
                                        DisplayEntity.BlockDisplayEntity.BLOCK_STATE.id,
                                        DisplayEntity.BlockDisplayEntity.BLOCK_STATE.dataType,
                                        Blocks.LIME_CONCRETE.defaultState
                                    ),
                                    DataTracker.SerializedEntry<Vector3f>(
                                        DisplayEntity.SCALE.id,
                                        DisplayEntity.SCALE.dataType,
                                        Vector3f(0.05f, 0.05f, length)
                                    ),
                                    DataTracker.SerializedEntry<Vector3f>(
                                        DisplayEntity.TRANSLATION.id,
                                        DisplayEntity.TRANSLATION.dataType,
                                        Vector3f(-0.025f, -0.025f, length / -2)
                                    )
                                )
                            )
                        )
                    )
                )

                activeIds += id
            }
        }
    }

    override fun onClose() {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(IntArrayList.wrap(activeIds.toIntArray())))
    }
}