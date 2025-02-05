package ua.mei.cinefabric.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.HotbarGui
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.entity.EntityType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.decoration.DisplayEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.BundleS2CPacket
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d
import ua.mei.cinefabric.scene.Keyframe
import java.util.*

class CutsceneGui(player: ServerPlayerEntity) : HotbarGui(player) {
    private val keyframes: MutableList<Keyframe> = mutableListOf()
    private val activeIds: MutableList<Int> = mutableListOf()

    private val elements: MutableList<Int> = mutableListOf()
    private var nextId: Int = -1

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
                    val id: Int = --nextId

                    player.networkHandler.sendPacket(
                        BundleS2CPacket(
                            listOf(
                                EntitySpawnS2CPacket(
                                    id,
                                    UUID.randomUUID(),
                                    player.x,
                                    player.y,
                                    player.z,
                                    0f,
                                    0f,
                                    EntityType.ITEM_DISPLAY,
                                    0,
                                    Vec3d.ZERO,
                                    0.0
                                ),
                                EntityTrackerUpdateS2CPacket(
                                    id,
                                    listOf(
                                        DataTracker.SerializedEntry<ItemStack>(
                                            DisplayEntity.ItemDisplayEntity.ITEM.id,
                                            DisplayEntity.ItemDisplayEntity.ITEM.dataType,
                                            Items.DIAMOND.defaultStack
                                        )
                                    )
                                )

                            )
                        )
                    )

                    elements += id
                }
        )
    }

    override fun onClose() {
        player.networkHandler.sendPacket(EntitiesDestroyS2CPacket(IntArrayList.wrap(elements.toIntArray())))
    }
}