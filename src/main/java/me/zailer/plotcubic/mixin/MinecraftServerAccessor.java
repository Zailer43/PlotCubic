package me.zailer.plotcubic.mixin;

import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("LOGGER")
    static Logger LOGGER() {
        throw new AssertionError();
    }
}
