package ua.mei.cinefabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object Cinefabric : ModInitializer {
    val cutscenesDir: Path = FabricLoader.getInstance().gameDir.resolve("cutscenes")

    override fun onInitialize() {
        if (!cutscenesDir.exists()) {
            cutscenesDir.createDirectories()
        }
    }
}
