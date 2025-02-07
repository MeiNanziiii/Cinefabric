package ua.mei.cinefabric.gui

import eu.pb4.sgui.api.elements.GuiElementBuilder
import eu.pb4.sgui.api.gui.HotbarGui
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import ua.mei.cinefabric.scene.Cutscene

class EditorGui(player: ServerPlayerEntity, val cutscene: Cutscene) : HotbarGui(player) {
    private val renderers: MutableList<KeyframeRenderer> = mutableListOf()
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
                    renderers += KeyframeRenderer(cutscene, keyframe, player) { --nextId }

                    render()
                }
        )

        setSlot(
            8,
            GuiElementBuilder.from(Items.EMERALD.defaultStack)
                .setCallback { ->
                    if (cutscene.keyframes.isNotEmpty()) {
                        close()
                        renderers.forEach { it.destroy() }

                        cutscene.play(player) {
                            render()
                            open()
                        }
                    }
                }
        )
    }

    fun render() {
        renderers.forEach {
            it.updateCamera()
            it.updateTrack()
        }
    }

    override fun onClose() {
        renderers.forEach { it.destroy() }
    }
}