package ua.mei.cinefabric

import com.mojang.brigadier.CommandDispatcher
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.server.command.CommandManager.RegistrationEnvironment
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import ua.mei.cinefabric.gui.CutsceneGui

object CinefabricCommands : CommandRegistrationCallback {
    override fun register(dispatcher: CommandDispatcher<ServerCommandSource>, access: CommandRegistryAccess, environment: RegistrationEnvironment) {
        dispatcher.register(
            literal("cutscene")
                .requires(Permissions.require("cutscene", 4))
                .then(
                    literal("create")
                        .requires(Permissions.require("cutscene.create", 4))
                        .executes { ctx ->
                            val player: ServerPlayerEntity = ctx.source.playerOrThrow

                            CutsceneGui(player).open()

                            1
                        }
                )
        )
    }
}