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

            if (index == 0 || duration <= 1) {
                listOf(
                    Frame(keyframe.x, keyframe.y, keyframe.z, keyframe.yaw, keyframe.pitch)
                )
            } else {
                val prev: Keyframe = keyframes[index - 1]

                var deltaYaw: Float = keyframe.yaw - prev.yaw
                if (deltaYaw > 180) deltaYaw -= 360
                if (deltaYaw < -180) deltaYaw += 360

                List(duration) { step ->
                    val factor: Float = keyframe.interpolation.func(step * (1f / (duration - 1)))

                    Frame(
                        prev.x + factor * (keyframe.x - prev.x),
                        prev.y + factor * (keyframe.y - prev.y),
                        prev.z + factor * (keyframe.z - prev.z),
                        prev.yaw + factor * deltaYaw,
                        prev.pitch + factor * (keyframe.pitch - prev.pitch)
                    )
                }
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