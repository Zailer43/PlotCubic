package me.zailer.plotcubic.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow public abstract ServerChunkManager getChunkManager();

    @Redirect(
            method = "canPlayerModifyAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;isSpawnProtected(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;)Z"
            )
    )
    private boolean isSpawnProtected(MinecraftServer instance, ServerWorld world, BlockPos pos, PlayerEntity player) {
        return false;
    }

    @Redirect(
            method = "tickWeather",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"
            )

    )
    private void dontSendRainPacketsToAllWorlds(PlayerManager instance, Packet<?> packet) {
        // Vanilla sends rain packets to all players when rain starts in a world,
        // even if they are not in it, meaning that if it is possible to rain in the world they are in,
        // the rain effect will remain until the player changes dimension or reconnects.
        instance.sendToDimension(packet, this.getChunkManager().getWorld().getRegistryKey());
    }
}
