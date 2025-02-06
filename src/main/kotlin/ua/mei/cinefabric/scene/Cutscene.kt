package ua.mei.cinefabric.scene

class Cutscene {
    val keyframes: MutableList<Keyframe> = mutableListOf()

    var frameRate: Int = 30
    var interpolation: Interpolation = Interpolation.LINEAR

    fun render(): List<Frame> {
        return keyframes.flatMapIndexed { index, keyframe ->
            val duration: Int = (keyframe.duration * (frameRate / 20f)).toInt()

            if (index == 0 || duration <= 1) {
                listOf(
                    Frame(keyframe.x, keyframe.y, keyframe.z, keyframe.yaw, keyframe.pitch)
                )
            } else {
                val prev: Keyframe = keyframes[index - 1]
                val invDuration: Float = 1f / (duration - 1)

                var deltaYaw: Float = keyframe.yaw - prev.yaw
                if (deltaYaw > 180) deltaYaw -= 360
                if (deltaYaw < -180) deltaYaw += 360

                List(duration) { step ->
                    val factor: Float = interpolation.func(step * invDuration)

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
        LINEAR({ t -> t }),
        EASE_IN({ t -> t * t }),
        EASE_OUT({ t -> 1 - (1 - t) * (1 - t) }),
        EASE_IN_OUT({ t ->
            if (t < 0.5f) 2 * t * t else -1 + (4 - 2 * t) * t
        })
    }

    data class Frame(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float)

    data class Keyframe(var x: Double, var y: Double, var z: Double, var yaw: Float, var pitch: Float, var duration: Int)
}