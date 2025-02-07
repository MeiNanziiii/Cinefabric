package ua.mei.cinefabric.scene

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.GameMode
import ua.mei.cinefabric.Cinefabric

class Cutscene {
    val keyframes: MutableList<Keyframe> = mutableListOf()
    var frameRate: Int = 30

    fun play(vararg players: ServerPlayerEntity, callback: (ServerPlayerEntity) -> Unit = {}) {
        val frames: List<Frame> = render()
        val playerStates: Map<ServerPlayerEntity, PlayerState> = players.associateWith { player ->
            PlayerState(
                player.x, player.y, player.z,
                player.yaw, player.pitch,
                player.interactionManager.gameMode
            )
        }

        players.forEach { it.changeGameMode(GameMode.SPECTATOR) }

        Cinefabric.launch {
            frames.forEach { frame ->
                players.forEach {
                    it.teleport(
                        it.serverWorld,
                        frame.x, frame.y, frame.z,
                        setOf(),
                        frame.yaw, frame.pitch,
                        false
                    )
                }
                delay(1000L / frameRate)
            }

            players.forEach { player ->
                val state: PlayerState = playerStates[player]!!

                player.teleport(
                    player.serverWorld,
                    state.x, state.y, state.z,
                    setOf(),
                    state.yaw, state.pitch,
                    false
                )
                player.changeGameMode(state.gameMode)

                callback(player)
            }
        }
    }

    fun render(): List<Frame> {
        return keyframes.flatMapIndexed { index, keyframe ->
            val duration: Int = (keyframe.duration * (frameRate / 20f)).toInt()
            val next: Keyframe = keyframes.getOrNull(index + 1) ?: keyframe
            val deltaYaw: Float = (next.yaw - keyframe.yaw).let {
                when {
                    it > 180 -> it - 360
                    it < -180 -> it + 360
                    else -> it
                }
            }

            List(duration) { step ->
                val factor: Float = keyframe.interpolation.func(step / (duration - 1f))

                Frame(
                    x = keyframe.x + factor * (next.x - keyframe.x),
                    y = keyframe.y + factor * (next.y - keyframe.y),
                    z = keyframe.z + factor * (next.z - keyframe.z),
                    yaw = keyframe.yaw + factor * deltaYaw,
                    pitch = keyframe.pitch + factor * (next.pitch - keyframe.pitch)
                )
            }
        }
    }

    enum class Interpolation(val func: (Float) -> Float) {
        NONE({ t -> if (t == 1f) 1f else 0f }),
        LINEAR({ t -> t }),
        EASE_IN({ t -> t * t }),
        EASE_OUT({ t -> 1 - (1 - t) * (1 - t) }),
        EASE_IN_OUT({ t -> t * t * (3 - 2 * t) })
    }

    data class Frame(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float)

    data class Keyframe(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float, var duration: Int, var interpolation: Interpolation)

    private data class PlayerState(val x: Double, val y: Double, val z: Double, val yaw: Float, val pitch: Float, val gameMode: GameMode)
}