package me.zailer.plotcubic.mixin;

import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionAccessor {

    @Mutable
    @Accessor("destructionType")
    void setDestructionType(Explosion.DestructionType destructionType);

    @Accessor("world")
    World getWorld();
}
