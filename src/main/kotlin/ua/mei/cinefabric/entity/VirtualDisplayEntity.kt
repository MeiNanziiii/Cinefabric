package ua.mei.cinefabric.entity

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.Brightness
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.entity.player.PlayerPosition
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import org.joml.Vector3d
import org.joml.Vector3f
import ua.mei.cinefabric.entity.data.VirtualDataTracker
import ua.mei.cinefabric.util.copy
import java.util.*

class VirtualDisplayEntity(val player: ServerPlayerEntity, val entityType: EntityType<*>) {
    val entityId: Int = Entity.CURRENT_ID.incrementAndGet()
    val dataTracker: VirtualDataTracker = VirtualDataTracker()

    var pos: Vector3d = Vector3d()
    private var lastSyncedPos: Vector3d = pos.copy()

    var rotation: Rotation = Rotation(0f, 0f)
    private var lastSyncedRotation: Rotation = rotation.copy()

    fun spawn() {
        player.networkHandler.sendPacket(
            BundleS2CPacket(
                buildList {
                    add(createSpawnPacket())
                    createSyncPackets(false).forEach(::add)
                }
            )
        )
    }

    fun sync() {
        player.networkHandler.sendPacket(BundleS2CPacket(createSyncPackets(true)))
    }

    fun destroy() {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(entityId))
    }

    fun setTranslation(x: Float, y: Float, z: Float) {
        dataTracker.set(DisplayEntity.TRANSLATION, Vector3f(x, y, z))
    }

    fun setScale(x: Float, y: Float, z: Float) {
        dataTracker.set(DisplayEntity.SCALE, Vector3f(x, y, z))
    }

    fun setBillboardMode(billboardMode: DisplayEntity.BillboardMode) {
        dataTracker.set(DisplayEntity.BILLBOARD, billboardMode.index)
    }

    fun setBrightness(brightness: Brightness) {
        dataTracker.set(DisplayEntity.BRIGHTNESS, brightness.pack())
    }

    fun createSpawnPacket(): EntitySpawnS2CPacket {
        lastSyncedPos = pos.copy()
        lastSyncedRotation = rotation.copy()

        return EntitySpawnS2CPacket(
            entityId,
            UUID.randomUUID(),
            pos.x, pos.y, pos.z,
            rotation.pitch, rotation.yaw,
            entityType,
            0,
            Vec3d.ZERO,
            0.0
        )
    }

    fun createSyncPackets(syncPos: Boolean): List<Packet<ClientPlayPacketListener>> {
        return buildList {
            if (dataTracker.isDirty()) {
                add(EntityTrackerUpdateS2CPacket(entityId, dataTracker.getDirtyEntries()))
            }

            if (syncPos && (lastSyncedPos != pos || lastSyncedRotation != rotation)) {
                lastSyncedPos = pos.copy()
                lastSyncedRotation = rotation.copy()

                add(EntityPositionSyncS2CPacket(entityId, PlayerPosition(Vec3d(pos.x, pos.y, pos.z), Vec3d.ZERO, rotation.yaw, rotation.pitch), false))
            }
        }
    }

    data class Rotation(var yaw: Float, var pitch: Float) {
        fun copy(): Rotation {
            return Rotation(yaw, pitch)
        }
    }
}