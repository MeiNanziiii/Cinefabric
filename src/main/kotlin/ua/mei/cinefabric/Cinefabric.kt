package ua.mei.cinefabric

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import kotlin.coroutines.CoroutineContext
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object Cinefabric : ModInitializer, CoroutineScope {
    val cutscenesDir: Path = FabricLoader.getInstance().gameDir.resolve("cutscenes")

    override fun onInitialize() {
        if (!cutscenesDir.exists()) {
            cutscenesDir.createDirectories()
        }

        CommandRegistrationCallback.EVENT.register(CinefabricCommands)
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default
}
