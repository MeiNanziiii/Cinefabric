package ua.mei.cinefabric.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.mei.cinefabric.cast.CinefabricPlayer;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setSneaking", at = @At("HEAD"))
    public void cinefabric$setSneaking(boolean bl, CallbackInfo ci) {
        if (this instanceof CinefabricPlayer player) {
            player.stopScene();
        }
    }
}
