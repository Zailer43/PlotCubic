package me.zailer.plotcubic.generator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.zailer.plotcubic.PlotManager;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PlotworldGenerator extends ChunkGenerator {
    public static final Codec<PlotworldGenerator> CODEC = RecordCodecBuilder.create((instance) ->
            method_41042(instance)
                    .and(
//                            PlotworldSettings.CODEC.fieldOf("settings").forGetter(PlotworldGenerator::getSettings)
//                    ).and(
                            RegistryOps.createRegistryCodec(Registry.BIOME_KEY).forGetter((generator) -> generator.biomeRegistry)
                    ).apply(instance, instance.stable(PlotworldGenerator::new)));

    private final Registry<Biome> biomeRegistry;

    public PlotworldGenerator(PlotworldSettings settings, Registry<Biome> biomeRegistry) {
        super(getEmptyStructureRegistry(), Optional.empty(), new FixedBiomeSource(settings.createBiome()), new FixedBiomeSource(biomeRegistry.getOrCreateEntry(BiomeKeys.PLAINS)), 0L);
        PlotManager.getInstance().setSettings(settings);
        this.biomeRegistry = biomeRegistry;
    }

    public PlotworldGenerator(Registry<StructureSet> structureSets, Registry<Biome> biomeRegistry) {
        this(PlotworldSettings.getDefaultConfig(biomeRegistry), biomeRegistry);
    }

    @SuppressWarnings("unchecked")
    public static Registry<StructureSet> getEmptyStructureRegistry() {
        MutableRegistry<StructureSet> structureSets = (MutableRegistry<StructureSet>) DynamicRegistryManager.createSimpleRegistry(Registry.STRUCTURE_SET_KEY);
        structureSets.clearTags();
        return structureSets;
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public ChunkGenerator withSeed(long seed) {
        return this;
    }

    @Override
    public MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler() {
        // Mirror what Vanilla does in the debug chunk generator
        return MultiNoiseUtil.method_40443();
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long l, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carver) {
    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structureAccessor, Chunk chunk) {
        PlotManager plotManager = PlotManager.getInstance();
        for (int x = 0; x != 16; x++) {
            for (int z = 0; z != 16; z++) {
                this.regen(plotManager, chunk, x, z);
            }
        }
    }

    public void regen(PlotManager plotManager, Chunk chunk, int x, int z) {
        for (int y = this.getMinimumY(); y != this.getWorldHeight(); y++) {
            BlockState block  = plotManager.getBlock(chunk.getPos().getBlockPos(x, y, z));

            chunk.setBlockState(new BlockPos(x, y, z), block, false);
        }
    }

    @Override
    public void populateEntities(ChunkRegion region) {
    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getWorldHeight() {
        return 320;
    }

    @Override
    public int getMinimumY() {
        return 0;
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType, HeightLimitView heightLimitView) {
        return 0;
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView heightLimitView) {
        return new VerticalBlockSample(0, new BlockState[0]);
    }

    @Override
    public void getDebugHudText(List<String> list, BlockPos blockPos) {
    }

    public static PlotworldGenerator createPlotGenerator(MinecraftServer server) {
        Registry<Biome> biomeRegistry = server.getRegistryManager().get(Registry.BIOME_KEY);
        PlotworldSettings settings = PlotworldSettings.getDefaultConfig(biomeRegistry);
        return new PlotworldGenerator(settings, biomeRegistry);
    }
}
