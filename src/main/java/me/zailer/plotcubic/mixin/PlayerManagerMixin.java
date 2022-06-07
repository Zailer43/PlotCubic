package me.zailer.plotcubic.mixin;

import me.zailer.plotcubic.PlotCubic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/util/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    private ServerWorld setWorld(MinecraftServer instance, RegistryKey<World> key) {
        if (PlotCubic.getConfig().general().autoTeleport()) {
            ServerWorld plotworld = PlotCubic.getPlotWorldHandle().asWorld();

            if (plotworld != null) {
                return plotworld;
            }
        }

        return instance.getWorld(key);
    }
}
