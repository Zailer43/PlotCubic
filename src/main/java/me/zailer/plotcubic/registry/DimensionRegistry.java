package me.zailer.plotcubic.registry;

import me.zailer.plotcubic.PlotCubic;
import me.zailer.plotcubic.generator.PlotworldGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class DimensionRegistry {

    public static void register() {
        Registry.register(Registry.CHUNK_GENERATOR, new Identifier(PlotCubic.MOD_ID, "plot"), PlotworldGenerator.CODEC);
    }
}
