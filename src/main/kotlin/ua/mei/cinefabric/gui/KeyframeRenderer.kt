package ua.mei.cinefabric.gui

import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.block.Blocks
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.decoration.Brightness
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.BundleS2CPacket
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.plus
import ua.mei.cinefabric.scene.Cutscene
import ua.mei.cinefabric.util.copy
import java.util.*
import kotlin.math.atan2
import kotlin.math.sqrt

class KeyframeRenderer(
    val cutscene: Cutscene,
    val keyframe: Cutscene.Keyframe,
    val player: ServerPlayerEntity,
    idGetter: () -> Int
) {
    val cameraId: Int = idGetter()
    val trackId: Int = idGetter()
    val textId: Int = idGetter()

    init {
        updateCamera()
        updateTrack()
    }

    fun updateCamera() {
        player.networkHandler.sendPacket(
            BundleS2CPacket(
                listOf(
                    spawnPacket(
                        cameraId,
                        keyframe.x, keyframe.y + 0.25f, keyframe.z,
                        keyframe.yaw + 180f, -keyframe.pitch,
                        EntityType.ITEM_DISPLAY
                    ),
                    EntityTrackerUpdateS2CPacket(
                        cameraId,
                        listOf(
                            getEntry(DisplayEntity.ItemDisplayEntity.ITEM, cameraHead),
                            getEntry(DisplayEntity.BRIGHTNESS, Brightness.FULL.pack())
                        )
                    )
                )
            )
        )
    }

    fun updateTrack() {
        val next: Cutscene.Keyframe = cutscene.keyframes.getOrNull(cutscene.keyframes.indexOf(keyframe) + 1) ?: keyframe

        val vector: Vector3d = Vector3d(keyframe.x, keyframe.y, keyframe.z)
        val nextVector: Vector3d = Vector3d(next.x, next.y, next.z)

        val midVector: Vector3d = vector.copy().plus(nextVector).mul(0.5)
        val diff: Vector3d = vector.copy().sub(nextVector)

        val length: Float = diff.length().toFloat()

        val yaw: Float = Math.toDegrees(atan2(-diff.x, diff.z)).toFloat()
        val pitch: Float = Math.toDegrees(atan2(diff.y, sqrt(diff.x * diff.x + diff.z * diff.z))).toFloat()

        player.networkHandler.sendPacket(
            BundleS2CPacket(
                listOf(
                    spawnPacket(
                        trackId,
                        midVector.x, midVector.y, midVector.z,
                        yaw + 180f, pitch,
                        EntityType.BLOCK_DISPLAY
                    ),
                    EntityTrackerUpdateS2CPacket(
                        trackId,
                        listOf(
                            getEntry(DisplayEntity.BlockDisplayEntity.BLOCK_STATE, Blocks.LIME_CONCRETE.defaultState),
                            getEntry(DisplayEntity.SCALE, Vector3f(0.0003f, 0.03f, length)),
                            getEntry(DisplayEntity.TRANSLATION, Vector3f(-0.00015f, -0.015f, length / -2)),
                            getEntry(DisplayEntity.BRIGHTNESS, Brightness.FULL.pack())
                        )
                    ),

                    spawnPacket(
                        textId,
                        midVector.x, midVector.y + 0.5f, midVector.z,
                        0f, 0f,
                        EntityType.TEXT_DISPLAY
                    ),
                    EntityTrackerUpdateS2CPacket(
                        textId,
                        listOf(
                            getEntry(DisplayEntity.TextDisplayEntity.TEXT, Text.literal("${keyframe.duration}t")),
                            getEntry(DisplayEntity.BILLBOARD, 3),
                            getEntry(DisplayEntity.BRIGHTNESS, Brightness.FULL.pack())
                        )
                    )
                )
            )
        )
    }

    fun destroy() {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(IntArrayList.wrap(listOf(cameraId, trackId, textId).toIntArray())))
    }

    private inline fun <reified T : Entity> spawnPacket(id: Int, x: Double, y: Double, z: Double, yaw: Float, pitch: Float, type: EntityType<T>): EntitySpawnS2CPacket {
        return EntitySpawnS2CPacket(
            id,
            UUID.randomUUID(),
            x, y, z,
            pitch, yaw,
            type,
            0,
            Vec3d.ZERO,
            0.0
        )
    }

    private inline fun <reified T> getEntry(data: TrackedData<T>, obj: T): DataTracker.SerializedEntry<T> {
        return DataTracker.SerializedEntry(data.id, data.dataType, obj)
    }

    companion object {
        val cameraHead: ItemStack = ItemStack(Items.PLAYER_HEAD).apply {
            set(
                DataComponentTypes.PROFILE,
                ProfileComponent(
                    Optional.empty(),
                    Optional.empty(),
                    PropertyMap().apply {
                        put("textures", Property("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNiNjExOGIxMWUwNTY0NjZhNWM2YzVkYzVlYjkyNmY3NGE1MGQ1Njk0OWIxYzIzNGFhZWMzOTlkMWE0OGRiMyJ9fX0="))
                    }
                )
            )
        }
    }
}