package ua.mei.cinefabric.entity.data

import net.minecraft.entity.data.DataTracker.Entry
import net.minecraft.entity.data.DataTracker.SerializedEntry
import net.minecraft.entity.data.TrackedData

class VirtualDataTracker {
    val entries = mutableMapOf<Int, Entry<*>>()

    private var dirty = false

    inline fun <reified T> get(data: TrackedData<T>): T? {
        return entries[data.id]?.get() as? T
    }

    fun <T> set(data: TrackedData<T>, obj: T) {
        val entry: Entry<T>? = entries[data.id] as? Entry<T>

        if (entry == null) {
            entries[data.id] = Entry(data, obj).apply { isDirty = true }
            dirty = true
        } else if (entry.get() != obj) {
            entry.set(obj)
            entry.isDirty = true
            dirty = true
        }
    }

    fun getDirtyEntries(): List<SerializedEntry<*>> {
        if (!dirty) return emptyList()

        dirty = false

        return entries.values
            .filter { it.isDirty }
            .onEach { it.isDirty = false }
            .map { it.toSerialized() }
    }

    fun isDirty(): Boolean {
        return dirty
    }
}