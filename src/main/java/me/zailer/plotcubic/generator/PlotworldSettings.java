package me.zailer.plotcubic.generator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;

import java.util.List;

public final class PlotworldSettings {
    private final int roadSize;
    private final int plotSize;
    private final int maxHeight;
    private final RegistryEntry<Biome> biome;
    private final BlockState roadBlock;
    private final BlockState unclaimedBorderBlock;
    private final BlockState claimedBorderBlock;
    private final BlockState borderSign;
    private final List<FlatChunkGeneratorLayer> layers;

    public PlotworldSettings(int roadSize, int plotSize, Biome biome, BlockState roadBlock, BlockState unclaimedBorderBlock, BlockState claimedBorderBlock, BlockState borderSign, List<FlatChunkGeneratorLayer> layers) {
        this.roadSize = roadSize;
        this.plotSize = plotSize;
        this.biome = RegistryEntry.of(biome);
        this.roadBlock = roadBlock;
        this.unclaimedBorderBlock = unclaimedBorderBlock;
        this.claimedBorderBlock = claimedBorderBlock;
        this.borderSign = borderSign;
        this.layers = layers;
        int height = 0;
        for (var layer : this.layers) {
            height += layer.getThickness();
        }
        this.maxHeight = height - 1;
    }

    public int getRoadSize() {
        return this.roadSize;
    }

    public int getPlotSize() {
        return this.plotSize;
    }

    public Biome getBiome() {
        return this.biome.value();
    }

    public BlockState getRoadBlock() {
        return this.roadBlock;
    }

    public BlockState getUnclaimedBorderBlock() {
        return this.unclaimedBorderBlock;
    }

    public BlockState getClaimedBorderBlock() {
        return this.claimedBorderBlock;
    }

    public BlockState getBorderSign() {
        return this.borderSign;
    }

    public int getMaxHeight() {
        return this.maxHeight;
    }

    public List<FlatChunkGeneratorLayer> getLayers() {
        return layers;
    }

    public RegistryEntry<Biome> createBiome() {
        Biome biome = this.getBiome();
        GenerationSettings.Builder builder = new GenerationSettings.Builder();

        return RegistryEntry.of(Biome.Builder.copy(biome).generationSettings(builder.build()).build());
    }

    public static PlotworldSettings getDefaultConfig(Registry<Biome> biomeRegistry) {
        return new PlotworldSettings(
                6,
                50,
                biomeRegistry.get(BiomeKeys.PLAINS),
                Blocks.SMOOTH_QUARTZ.getDefaultState(),
                Blocks.STONE_SLAB.getDefaultState(),
                Blocks.CUT_COPPER_SLAB.getDefaultState(),
                Blocks.DARK_OAK_WALL_SIGN.getDefaultState(),
                List.of(new FlatChunkGeneratorLayer(1, Blocks.BARRIER), new FlatChunkGeneratorLayer(49, Blocks.STONE), new FlatChunkGeneratorLayer(1, Blocks.GRASS_BLOCK))
        );
    }
}
