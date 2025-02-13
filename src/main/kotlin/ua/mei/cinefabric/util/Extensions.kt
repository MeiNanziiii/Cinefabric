package ua.mei.cinefabric.util

import net.minecraft.component.ComponentType
import net.minecraft.item.ItemStack
import org.joml.Vector3d

fun Vector3d.copy(): Vector3d = Vector3d(this.x, this.y, this.z)

inline fun <reified T> ItemStack.with(componentType: ComponentType<in T>, value: T): ItemStack {
    this.set(componentType, value)
    return this
}
