package ua.mei.cinefabric.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import ua.mei.cinefabric.cast.CinefabricPlayer;
import ua.mei.cinefabric.scene.Cutscene;

import java.util.Set;

@SuppressWarnings("ConstantConditions")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements CinefabricPlayer {
    @Unique
    private final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    @Unique
    private @Nullable Double prevX;
    @Unique
    private @Nullable Double prevY;
    @Unique
    private @Nullable Double prevZ;

    @Unique
    private @Nullable Float prevYaw;
    @Unique
    private @Nullable Float prevPitch;

    @Unique
    private @Nullable GameMode prevGamemode;

    @Unique
    private @Nullable Cutscene currentScene;
    @Unique
    private @Nullable Runnable callback;

    @Unique
    private @Nullable Thread loop;

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Override
    public @Nullable Cutscene getScene() {
        return this.currentScene;
    }

    @Override
    public void playScene(Cutscene scene, Runnable callback) {
        this.prevX = player.getX();
        this.prevY = player.getY();
        this.prevZ = player.getZ();

        this.prevYaw = player.getHeadYaw();
        this.prevPitch = player.getPitch();

        this.prevGamemode = player.interactionManager.getGameMode();

        this.currentScene = scene;
        this.callback = callback;

        this.loop = new Thread() {
            @Override
            public void run() {
                ServerPlayerEntityMixin.this.currentScene.render().forEach(frame -> {
                    if (Thread.interrupted()) return;

                    player.teleport(
                            getServerWorld(),
                            frame.getX(), frame.getY(), frame.getZ(),
                            Set.of(),
                            frame.getYaw(), frame.getPitch(),
                            false
                    );

                    try {
                        Thread.sleep(1000L / ServerPlayerEntityMixin.this.currentScene.getFrameRate());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                if (!this.isInterrupted()) {
                    stopScene();
                }
            }
        };
        this.loop.start();

        player.changeGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void stopScene() {
        if (this.currentScene == null) return;

        this.loop.interrupt();
        this.loop = null;
        this.currentScene = null;

        player.teleport(
                getServerWorld(),
                prevX, prevY, prevZ,
                Set.of(),
                prevYaw, prevPitch,
                false
        );
        player.changeGameMode(prevGamemode);

        this.callback.run();
    }
}
